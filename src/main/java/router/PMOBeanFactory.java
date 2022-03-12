package router;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.FileSystemResource;

import plugins.Plugin;

/*
 * REturn a plugin from the Spring beanfactory
 *   and the xml definition 
 */
public class PMOBeanFactory {

	public PMOBeanFactory() {
	
		// i'll think of something
	}
	
	public Plugin  getPlugin(String pluginName) {
		
		

		
		 
		//System.out.println(getClassPath());
		

		/*
		 * using FileSystem.... not as good as ClassPath, but at least it works.
		 */
				
		// BeanFactory bf = new XmlBeanFactory(
        //         new FileSystemResource("/home/arne/workspace/pmo/WebContent/META-INF/spring/" + pluginName + ".xml"));

		/*
		 * using ClassPathResource..
		 */
		System.out.println("locating bean in classpath!");
		
		BeanFactory bf = new XmlBeanFactory(
                 new ClassPathResource("beans/" + pluginName + ".xml"));

		 
		 Plugin plugin = (Plugin) bf.getBean(pluginName);
		
		 return plugin;
		 
		 
	}
	
	
}
