package eu.trentorise.smartcampus.resourceprovider.uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.util.UriTemplate;

import eu.trentorise.smartcampus.resourceprovider.jaxbmodel.ResourceDeclaration;
import eu.trentorise.smartcampus.resourceprovider.jaxbmodel.ResourceMapping;
import eu.trentorise.smartcampus.resourceprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.resourceprovider.util.HttpMethod;

/**
 * Helper class to map the resource requests onto the unique resource IDs (resource URIs).
 * The mapping is based on the (XML) resource descriptor provided.
 * @author federico
 * 
 */
public class UriManager {

	private final static Log logger = LogFactory.getLog(UriManager.class);

	private Service service;
	
	public UriManager(InputStream is) {
		super();
		service = loadResourceTemplates(is);
	}

	/**
	 * @param tagProviderFile
	 */
	public UriManager(File tagProviderFile) {
		super();
		try {
			service = loadResourceTemplates(new FileInputStream(tagProviderFile));
		} catch (FileNotFoundException e) {
			logger.error("Failed to find resource file: "+e.getMessage(), e);
			e.printStackTrace();
		}
	}


	public String getUriFromRequest(String url, HttpMethod method, Collection<GrantedAuthority> collection) {

		if (service != null) {
			List<ResourceMapping> listPath = service.getResourceMapping();
			Iterator<ResourceMapping> index = listPath.iterator();
			while (index.hasNext()) {
				ResourceMapping rm = index.next();
				UriTemplate pathPattern = rm.getPathTemplate();
				//System.out.println(collection.toString());
				//System.out.println(rm.getAuthority());
				//System.out.println(pattern);
			//	if (collection.toString().contains(rm.getAuthority())) {
				if (pathPattern.matches(url) && (rm.getMethod()==null || rm.getMethods().contains(method.toString()))) {
					Map<String, String> match = pathPattern.match(url);
					logger.info("Check  " + pathPattern +" " +rm.getMethod()+" ==> " + match );
					return rm.getUriTemplate().expand(match).toString();
				}
			}
		}else{
			logger.info("Service non registered ");
		}
		logger.info("Resource not exist on this service: " + url);
		return null;

	}

	private Service loadResourceTemplates(InputStream inputStream) {
		try {
			JAXBContext jaxb = JAXBContext.newInstance(Service.class,
					Service.class, ResourceMapping.class,
					ResourceDeclaration.class);
			Unmarshaller unm = jaxb.createUnmarshaller();
			JAXBElement<Service> element = (JAXBElement<Service>) unm
					.unmarshal(new StreamSource(inputStream), Service.class);
			return element.getValue();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(
					"Failed to load resource templates: " + e.getMessage(), e);
			return null;
		}
	}

}
