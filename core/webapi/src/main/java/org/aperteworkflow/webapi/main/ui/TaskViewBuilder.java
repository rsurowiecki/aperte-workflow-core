package org.aperteworkflow.webapi.main.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetAttribute;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.IHtmlTemplateProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Html builder for the task view 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class TaskViewBuilder 
{
	private BpmTask task;
	private List<ProcessStateWidget> widgets;
	private List<ProcessStateAction> actions;
	private I18NSource i18Source;
	private UserData user;
	
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
	@Autowired
	private IHtmlTemplateProvider templateProvider;
	
	/** Builder for javascripts */
	private StringBuilder scriptBuilder = new StringBuilder("<script type=\"text/javascript\">");
	
	private int vaadinWidgetsCount = 0;
	
	public TaskViewBuilder()
	{
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	public void processView(PrintWriter printWriter) throws IOException
	{
		Document document = Jsoup.parse("");
		
		Element alertsNode = document.createElement("div")
				.attr("id", "alerts-list")
				.attr("class", "process-alerts");
		document.appendChild(alertsNode);
		
		Element widgetsNode = document.createElement("div")
				.attr("id", "vaadin-widgets")
				.attr("class", "vaadin-widgets-view");
		document.appendChild(widgetsNode);
		
		for(ProcessStateWidget widget: widgets)
		{
			processWidget(widget, widgetsNode);
		}
		
		Element actionsNode = document.createElement("div")
				.attr("id", "actions-list")
				.attr("class", "actions-view");
		document.appendChild(actionsNode);
		
		addSaveActionButton(actionsNode);
		addCancelActionButton(actionsNode);
		
		for(ProcessStateAction action: actions)
		{
			processAction(action, actionsNode);
		}
		
		
		
		printWriter.print(document.toString());
		
		scriptBuilder.append("vaadinWidgetsCount = ");
		scriptBuilder.append(vaadinWidgetsCount);
		scriptBuilder.append(";");
		scriptBuilder.append("</script>");
		printWriter.print(scriptBuilder.toString());
		
	}
	
	private void processWidget(ProcessStateWidget widget, Element parent)
	{
		String aliasName = widget.getClassName();
		
		/* Sort widgets by prority */
		List<ProcessStateWidget> children = new ArrayList<ProcessStateWidget>(widget.getChildren());
		Collections.sort(children, new Comparator<ProcessStateWidget>() {

			@Override
			public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
				// TODO Auto-generated method stub
				return widget1.getPriority().compareTo(widget2.getPriority());
			}
		});
		
		/* Check if widget is based on html */
		String widgetTemplateBody = templateProvider.getTemplate(aliasName);
		
		if(aliasName.equals("TabSheet"))
		{
			String tabId = "tab_sheet_"+widget.getId();
			String divContentId = "div_content_"+widget.getId();

			Element ulNode = parent.ownerDocument().createElement("ul")
				.attr("id", tabId)
				.attr("class", "nav nav-tabs");
			parent.appendChild(ulNode);
			
			Element divContentNode = parent.ownerDocument().createElement("div")
				.attr("id", divContentId)
				.attr("class", "tab-content");
			parent.appendChild(divContentNode);
			
			boolean isFirst = true;

			for(ProcessStateWidget child: children)
			{
				String caption = aliasName;
				/* Set caption from attributes */
				ProcessStateWidgetAttribute attribute = child.getAttributeByName("caption");
				if(attribute != null)
					caption = i18Source.getMessage(attribute.getValue());
				
				String childId = "tab" + child.getId();
				
				/* Li tab element */
				Element liNode = parent.ownerDocument().createElement("li");
				ulNode.appendChild(liNode);
				
				Element aNode = parent.ownerDocument().createElement("a")
						.attr("id", "tab_link_"+childId)
						.attr("href", "#"+childId)
						.attr("data-toggle", "tab")
						.append(caption);
				
				liNode.appendChild(aNode);
				
				scriptBuilder.append("$('#tab_link_"+childId+"').on('shown', function (e) { onTabChange(e); });");
				
				/* Content element */
				Element divTabContentNode = parent.ownerDocument().createElement("div")
						.attr("id", childId)
						.attr("class", isFirst ? "tab-pane active" : "tab-pane");
				divContentNode.appendChild(divTabContentNode);
				
				if(isFirst)
					isFirst = false;
				
				processWidget(child, divTabContentNode);
			}
			
			scriptBuilder.append("$('#"+tabId+" a:first').tab('show');");
			
			
		}
		else if(aliasName.equals("VerticalLayout"))
		{
			Element divContentNode = parent.ownerDocument().createElement("div")
				.attr("id", "vertical_layout"+widget.getId());
			parent.appendChild(divContentNode);
			
			for(ProcessStateWidget child: children)
			{
				processWidget(child, divContentNode);
			}
		}
		else if(widgetTemplateBody != null)
		{
			Map<String, Object> viewData = new HashMap<String, Object>();
			viewData.put(IHtmlTemplateProvider.PROCESS_PARAMTER, task.getProcessInstance());
			viewData.put(IHtmlTemplateProvider.TASK_PARAMTER, task);
			viewData.put(IHtmlTemplateProvider.USER_PARAMTER, user);
			viewData.put(IHtmlTemplateProvider.MESSAGE_SOURCE_PARAMETER, i18Source);
			viewData.put(IHtmlTemplateProvider.WIDGET_NAME_PARAMETER, aliasName);
			viewData.put(IHtmlTemplateProvider.PERMISSIONS_PARAMETER, widget.getPermissions());
			viewData.put(IHtmlTemplateProvider.WIDGET_ID_PARAMETER, widget.getId().toString());
			
			String processedView = templateProvider.processTemplate(aliasName, viewData);
			
			Element divContentNode = parent.ownerDocument().createElement("div")
					.append(processedView)
					.attr("class", "html-widget-view")
					.attr("id", "html-"+widget.getId());
				parent.appendChild(divContentNode);
				
			for(ProcessStateWidget child: children)
			{
				processWidget(child, divContentNode);
			}
			
		}
		else
		{
			vaadinWidgetsCount++;
			//http://localhost:8080
			String vaadinWidgetUrl = "/aperteworkflow/widget/"+task.getInternalTaskId()+"_"+widget.getId()+"/?widgetId=" + widget.getId() + "&taskId="+task.getInternalTaskId();
			 
			Element iFrameNode = parent.ownerDocument().createElement("iframe")
					.attr("src", vaadinWidgetUrl)
					.attr("autoResize", "true")
					.attr("id", "iframe-vaadin-"+widget.getId())
					.attr("frameborder", "0")
					.attr("taskId", task.getInternalTaskId())
					.attr("widgetId", widget.getId().toString())
					.attr("class", "vaadin-widget-view")
					.attr("widgetLoaded", "false")
					.attr("name", widget.getId().toString());
			parent.appendChild(iFrameNode);
			
			scriptBuilder.append("$('#iframe-vaadin-"+widget.getId()+"').load(function() {onLoadIFrame($(this)); });");

			
			for(ProcessStateWidget child: children)
			{
				processWidget(child, iFrameNode);
			}
		}
	}
	
	private void processAction(ProcessStateAction action, Element parent)
	{
		String actionButtonId = "action-button-" + action.getBpmName();
		
		Element buttonNode = parent.ownerDocument().createElement("button")
				.appendText(i18Source.getMessage(action.getLabel()))
				.attr("class", "btn aperte-button")
				.attr("disabled", "true")
				.attr("type", "button")
				.attr("id", actionButtonId);
			parent.appendChild(buttonNode);
			
			scriptBuilder.append("$('#" + actionButtonId+"').click(function() { disableButtons(); performAction(this, '"+action.getBpmName()+
					"', "+action.getSkipSaving()+", '"+task.getInternalTaskId()+"');  });");
			scriptBuilder.append("$('#" + actionButtonId+"').tooltip({title: '"+i18Source.getMessage(action.getDescription())+"'});");
	}
	
	private void addSaveActionButton(Element parent)
	{
		String actionButtonId = "action-button-save";
		
		Element buttonNode = parent.ownerDocument().createElement("button")
				.appendText(i18Source.getMessage("button.save.process.data"))
				.attr("class", "btn btn-success aperte-button")
				.attr("disabled", "true")
				.attr("type", "button")
				.attr("id", actionButtonId);
			parent.appendChild(buttonNode);
			
			scriptBuilder.append("$('#" + actionButtonId+"').click(function() { onSaveButton('"+task.getInternalTaskId()+"');  });");
			scriptBuilder.append("$('#" + actionButtonId+"').tooltip({title: '"+i18Source.getMessage("button.save.process.desc")+"'});");
	}
	
	private void addCancelActionButton(Element parent)
	{
		String actionButtonId = "action-button-cancel";
		
		Element buttonNode = parent.ownerDocument().createElement("button")
				.appendText(i18Source.getMessage("button.cancel"))
				.attr("class", "btn btn-inverse aperte-button")
				.attr("disabled", "true")
				.attr("type", "button")
				.attr("id", actionButtonId);
			parent.appendChild(buttonNode);
			
			scriptBuilder.append("$('#" + actionButtonId+"').click(function() { onCancelButton();  });");
			scriptBuilder.append("$('#" + actionButtonId+"').tooltip({title: '"+i18Source.getMessage("button.cancel")+"'});");
	}

	public TaskViewBuilder setWidgets(List<ProcessStateWidget> widgets) 
	{
		this.widgets = widgets;
		
		return this;
	}

	public TaskViewBuilder setActions(List<ProcessStateAction> actions) 
	{
		this.actions = actions;
		
		return this;
	}
	
	public TaskViewBuilder setI18Source(I18NSource i18Source) 
	{
		this.i18Source = i18Source;
		
		return this;
	}

	public TaskViewBuilder setTask(BpmTask task) 
	{
		this.task = task;
		return this;
	}

	public TaskViewBuilder setUser(UserData user) 
	{
		this.user = user;
		return this;
	}

}
