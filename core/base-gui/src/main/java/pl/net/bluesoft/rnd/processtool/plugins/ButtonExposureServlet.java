package pl.net.bluesoft.rnd.processtool.plugins;

import com.thoughtworks.xstream.XStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

public class ButtonExposureServlet extends HttpServlet {
	public enum Format {
		JSON, XML
	}

	private static Logger				logger			= Logger.getLogger(ButtonExposureServlet.class.getName());
	private static final ObjectMapper	mapper			= new ObjectMapper();
	private static final XStream		xstream			= new XStream();
	private static final Format			DEFAULT_FORMAT	= Format.JSON;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");

		List<String> steps = new ArrayList<String>();

        for (String buttonAlias : getRegistry().getGuiRegistry().getAvailableButtons().keySet()) {
           steps.add(buttonAlias);
        }

        PrintWriter out = resp.getWriter();
		String formatString = req.getParameter("format");
		Format format;
		if (formatString == null) {
			format = DEFAULT_FORMAT;
		} else {
			format = Format.valueOf(formatString.toUpperCase());
		}

		switch (format) {
			case XML: {
				out.write(xstream.toXML(steps));
				break;
			}
			case JSON: {
				mapper.configure(Feature.INDENT_OUTPUT, true);
				mapper.writeValue(out, steps);
				break;
			}
		}

		out.close();

		logger.info(this.getClass().getSimpleName() + " GET");
	}

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info(this.getClass().getSimpleName() + " INITIALIZED: " + getServletContext().getContextPath());
	}

	@Override
	public void destroy() {
		super.destroy();
		logger.info(this.getClass().getSimpleName() + " DESTROYED");
	}
}
