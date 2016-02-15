package utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import controller.Controller;

public class BuildTime
{
	private static BuildTime instance;
	
	private long bufgetBuildDateTime = 0;

	public static BuildTime getInstance()
	{
		if(instance == null)
		{
			instance = new BuildTime();
		}
		
		return instance;
	}
	
	public BuildTime()
	{
		this.getBuildTimestamp();
	}
	
	public String getBuildDateTimeString(){
		return DateTimeFormat.timestamptoString(this.getBuildTimestamp(), "yyyy-MM-dd HH:mm:ss z", "UTC");
	}
	
	public long getBuildTimestamp(){
		if(bufgetBuildDateTime == 0)
	    {
			bufgetBuildDateTime =  getClassBuildTime().getTime(); 
	    } 
	    return bufgetBuildDateTime;
	}

	/**
	 * Handles files, jar entries, and deployed jar entries in a zip file (EAR).
	 * @return The date if it can be determined, or null if not.
	 */
	private static Date getClassBuildTime() {
	    Date d = null;
	    Class<?> currentClass = Controller.class;
	    URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
	    if (resource != null) {
	        if (resource.getProtocol().equals("file")) {
	            try {
	                d = new Date(new File(resource.toURI()).lastModified());
	            } catch (URISyntaxException ignored) { }
	        } else if (resource.getProtocol().equals("jar")) {
	            String path = resource.getPath();
	            d = new Date( new File(path.substring(5, path.indexOf("!"))).lastModified() );    
	        } else if (resource.getProtocol().equals("zip")) {
	            String path = resource.getPath();
	            File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
	            //long jfodLastModifiedLong = jarFileOnDisk.lastModified ();
	            //Date jfodLasModifiedDate = new Date(jfodLastModifiedLong);
	            try(JarFile jf = new JarFile (jarFileOnDisk)) {
	                ZipEntry ze = jf.getEntry (path.substring(path.indexOf("!") + 2));//Skip the ! and the /
	                long zeTimeLong = ze.getTime ();
	                Date zeTimeDate = new Date(zeTimeLong);
	                d = zeTimeDate;
	            } catch (IOException|RuntimeException ignored) { }
	        }
	    }
	    return d;
	}
}