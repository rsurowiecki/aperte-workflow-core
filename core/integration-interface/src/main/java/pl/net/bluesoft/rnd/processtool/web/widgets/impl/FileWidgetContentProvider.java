package pl.net.bluesoft.rnd.processtool.web.widgets.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetContentProvider;

/**
 * Widget content provider based on file 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class FileWidgetContentProvider implements IWidgetContentProvider {

	private String fileName;
	private IBundleResourceProvider bundleResourceProvider;
	
	public FileWidgetContentProvider(String fileName, IBundleResourceProvider bundleResourceProvider)
	{
		this.fileName = fileName;
		this.bundleResourceProvider = bundleResourceProvider;
	}

	@Override
	public InputStream getHtmlContent() 
	{
		try
		{
			return bundleResourceProvider.getBundleResourceStream(fileName);
			
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Exception during html file opening: "+fileName, ex);
		}
	}

	@Override
	public String getFileName()
	{
		return this.fileName;
	}

}