package utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class PebbleHelper {
	
	
	private PebbleTemplate template;
	private Map<String, Object> contextMap;

	private PebbleHelper(PebbleTemplate template, Map<String, Object> contextMap)
	{
		this.contextMap = contextMap;
		this.template = template;
		
	}
	
	
	public static PebbleHelper getPebbleHelper(String htmlTemplate) throws PebbleException
	{
		PebbleEngine engine = new PebbleEngine();
		EscaperExtension escaper = engine.getExtension(EscaperExtension.class);
		escaper.setAutoEscaping(false);
		PebbleTemplate compiledTemplate = engine.getTemplate(htmlTemplate);
		return new PebbleHelper(compiledTemplate, new HashMap<String, Object>());
		
	}
	
	
	public String evaluate() throws PebbleException
	{
		try(Writer writer = new StringWriter();)
		{
			template.evaluate(writer, contextMap);
			return writer.toString();
		} catch (IOException e) {
			throw new PebbleException(e, e.getMessage());
		}
	}


	public Map<String, Object> getContextMap() {
		return contextMap;
	}

	
}