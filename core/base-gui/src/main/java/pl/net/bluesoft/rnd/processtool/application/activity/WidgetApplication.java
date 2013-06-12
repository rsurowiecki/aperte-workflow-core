package pl.net.bluesoft.rnd.processtool.application.activity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.event.SaveTaskEvent;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;

/**
 * Activity application standalone version to use outside portal portlet and
 * for fast link process view
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */

public class WidgetApplication extends Application  implements HttpServletRequestListener
{
	protected I18NSource i18NSource;
	protected Locale locale = null;
	
	private static Logger logger = Logger.getLogger(WidgetApplication.class.getName());
    
    private boolean initialized = false;

    @Autowired
    private ProcessToolRegistry processToolRegistry;
    
    @Autowired
    private EventBus eventBus;
    
    private Window blankWindow;
    
    private Map<String, WidgetViewWindow> widgetWindows;
    
    private ProcessToolBpmSession bpmSession;

    
    public WidgetApplication()
    {
    	this.i18NSource = I18NSourceFactory.createI18NSource(Locale.getDefault());
    	this.widgetWindows = new HashMap<String, WidgetViewWindow>();
    }

    @Subscribe
    public void listen(final SaveTaskEvent event)
    {
    	logger.warning("Perform widget save for taskId: "+event.getTaskId()+" windows: "+widgetWindows.size()+" name: "+this);
    	
    	processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() 
    	{
	
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				BpmTask task = bpmSession.getTaskData(event.getTaskId(), ctx);
				
				for(WidgetViewWindow window: widgetWindows.values())
					window.saveWidgets(event, task);
			}
		});
    }
    
   
	@Override
	public synchronized Window getWindow(String name) 
	{
		logger.log(Level.WARNING, "name: "+name+"app: "+this);
		
		WebApplicationContext context = (WebApplicationContext)this.getContext();
		if(context == null)
		{
			logger.log(Level.WARNING, "no context...");
			if(blankWindow == null)
			{
				blankWindow = new Window();
				setMainWindow(blankWindow);
			}
			
			return blankWindow;
		}
		
		RequestParameters requestParameters = analyseWindowName(name);
		if(requestParameters == null)
		{
			logger.log(Level.WARNING, "no parameters...");
			return null;
		}
		
		if(requestParameters.getClose())
		{
			logger.log(Level.WARNING, "close...");
			for(WidgetViewWindow windowToDestory: widgetWindows.values())
			{
				logger.log(Level.WARNING, "remove window: "+windowToDestory.getName());
				windowToDestory.destroy();
				removeWindow(windowToDestory);
			}
			widgetWindows.clear();
			return null;
		} 

		if(bpmSession == null)
			bpmSession = (ProcessToolBpmSession)context.getHttpSession().getAttribute(ProcessToolBpmSession.class.getName());
		
		WidgetViewWindow window = widgetWindows.get(requestParameters.getWindowName());
		
		/* Window for specified tab with given name already exists, return it */
		if(window != null)
		{
			if(!this.getWindows().contains(window))
				addWindow(window);
			
			window.initlizeWidget(requestParameters.getTaskId(), requestParameters.getWidgetId());
			logger.log(Level.WARNING, "return window! "+window.isVisible());
			return window;
		}
		
		if(i18NSource == null)
			this.i18NSource = I18NSourceFactory.createI18NSource(Locale.getDefault());
		
		
		/* New tab was opened, create new window for it */
		WidgetViewWindow newWindow = new WidgetViewWindow(processToolRegistry, bpmSession, this, i18NSource);
		newWindow.setSizeFull();
		newWindow.setName(requestParameters.getWindowName());
		newWindow.initlizeWidget(requestParameters.getTaskId(), requestParameters.getWidgetId());
		
		widgetWindows.put(newWindow.getName(), newWindow);
		
		logger.log(Level.WARNING, "New window created: "+newWindow.getName());
		
		addWindow(newWindow);
		//newWindow.open(new ExternalResource(newWindow.getURL()));

		return newWindow; 
	}



	@Override
	public void onRequestStart(final HttpServletRequest request, HttpServletResponse response) 
	{	
		if(!initialized)
			init();
		
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
			
		if(ctx == null)
		{
			UserData user = (UserData)request.getSession().getAttribute(UserData.class.getName());
			if(user == null)
			{
				processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
					
					@Override
					public void withContext(ProcessToolContext ctx) 
					{
						IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
						
						UserData user = authorizationService.getUserByRequest(request);
						setUser(user);
						
						request.getSession().setAttribute(UserData.class.getName(), user);
						
						ProcessToolBpmSession bpmSession =  (ProcessToolBpmSession) request.getSession().getAttribute(ProcessToolBpmSession.class.getName()); 
						if(bpmSession == null)
						{
							bpmSession = ctx.getProcessToolSessionFactory().createSession(user, user.getRoleNames());
							request.getSession().setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
						}
						
						WidgetApplication.this.i18NSource = I18NSourceFactory.createI18NSource(request.getLocale());
						
					}
				});
			}
			

		}
	}
	
	@Override
	public void onRequestEnd(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}



	public void init() 
	{
		synchronized (this) 
		{
			if(initialized)
				return;
			
			initialized = true;
			
			setMainWindow(getWindow(null));
			
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
			
			this.eventBus.register(this);
			
			logger.warning("init app: "+this);
		}
		

		
	}
	
	
	@Override
	public void close() 
	{
		try
		{
			logger.warning("closing... widgets: "+widgetWindows.size()+", app: "+this);
			for(WidgetViewWindow window: widgetWindows.values())
			{
				window.destroy();
				removeWindow(window);
			}
			
			widgetWindows.clear();
			
			/* We have to unregister view form bus */
			eventBus.unregister(this);
		}
		catch(IllegalArgumentException ex)
		{
			logger.warning("ups...: ");
		}
		super.close();
	}
	
	/** Analyse given widnow name by Vaadin framework to get taskId, widgetId
	 * and information if the window should be closed
	 * @param windowRequestName
	 * @return
	 */
	private RequestParameters analyseWindowName(String windowRequestName)
	{
		String[] parameters = windowRequestName.split("_");
		
		if(parameters == null || parameters.length < 2)
		{
			logger.severe("Invalid window name: "+windowRequestName);
			return null;
		}
		
		Boolean close = false;
		String taskId = parameters[0];
		String widgetId = parameters[1];
		
		String windowName = taskId+"_"+widgetId;
		
		if(parameters.length == 3)
			close = true;
		
		RequestParameters requestParameters = new RequestParameters();
		requestParameters.setClose(close);
		requestParameters.setTaskId(taskId);
		requestParameters.setWidgetId(widgetId);
		requestParameters.setWindowName(windowName);
		
		return requestParameters;
	}
	
	/** Class to encapsulate window name analysis */
	public static class RequestParameters
	{
		private String windowName;
		private String taskId;
		private String widgetId;
		private Boolean close;
		
		public String getWindowName() {
			return windowName;
		}
		public void setWindowName(String windowRequestName) {
			this.windowName = windowRequestName;
		}
		public String getTaskId() {
			return taskId;
		}
		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}
		public String getWidgetId() {
			return widgetId;
		}
		public void setWidgetId(String widgetId) {
			this.widgetId = widgetId;
		}
		public Boolean getClose() {
			return close;
		}
		public void setClose(Boolean close) {
			this.close = close;
		}
		
		
	}
}
