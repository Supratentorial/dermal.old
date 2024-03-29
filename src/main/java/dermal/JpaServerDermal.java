package dermal;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.dstu3.JpaConformanceProviderDstu3;
import ca.uhn.fhir.jpa.provider.dstu3.JpaSystemProviderDstu3;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Meta;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JpaServerDermal extends RestfulServer {

    private WebApplicationContext context;

    protected void initialise() throws ServletException {
        super.initialize();

        FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
        setFhirContext(new FhirContext(fhirVersion));

        // Get the spring context from the web container (it's declared in web.xml)
        context = ContextLoaderListener.getCurrentWebApplicationContext();

		/*
         * The BaseJavaConfigDstu2.java class is a spring configuration
		 * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
		 * contains bean definitions for a resource provider for each resource type
		 */
        String resourceProviderBeanName;
        if (fhirVersion == FhirVersionEnum.DSTU1) {
            resourceProviderBeanName = "myResourceProvidersDstu1";
        } else if (fhirVersion == FhirVersionEnum.DSTU2) {
            resourceProviderBeanName = "myResourceProvidersDstu2";
        } else if (fhirVersion == FhirVersionEnum.DSTU3) {
            resourceProviderBeanName = "myResourceProvidersDstu3";
        } else {
            throw new IllegalStateException();
        }
        List<IResourceProvider> beans = context.getBean(resourceProviderBeanName, List.class);
        setResourceProviders(beans);

		/*
         * The system provider implements non-resource-type methods, such as
		 * transaction, and global history.
		 */
        Object systemProvider;
        if (fhirVersion == FhirVersionEnum.DSTU3) {
            systemProvider = context.getBean("mySystemProviderDstu3", JpaSystemProviderDstu3.class);
        } else {
            throw new IllegalStateException();
        }
        setPlainProviders(systemProvider);

		/*
		 * The conformance provider exports the supported resources, search parameters, etc for
		 * this server. The JPA version adds resource counts to the exported statement, so it
		 * is a nice addition.
		 */
        if (fhirVersion == FhirVersionEnum.DSTU3) {
            IFhirSystemDao<Bundle, Meta> systemDao = context.getBean("mySystemDaoDstu3", IFhirSystemDao.class);
            JpaConformanceProviderDstu3 confProvider = new JpaConformanceProviderDstu3(this, systemDao,
                    context.getBean(DaoConfig.class));
            confProvider.setImplementationDescription("Example Server");
            setServerConformanceProvider(confProvider);
        } else {
            throw new IllegalStateException();
        }

		/*
		 * Enable ETag Support (this is already the default)
		 */
        setETagSupport(ETagSupportEnum.ENABLED);

		/*
		 * This server tries to dynamically generate narratives
		 */
        FhirContext ctx = getFhirContext();
        ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());

		/*
		 * Default to JSON and pretty printing
		 */
        setDefaultPrettyPrint(true);
        setDefaultResponseEncoding(EncodingEnum.JSON);

		/*
		 * -- New in HAPI FHIR 1.5 --
		 * This configures the server to page search results to and from
		 * the database, instead of only paging them to memory. This may mean
		 * a performance hit when performing searches that return lots of results,
		 * but makes the server much more scalable.
		 */
        setPagingProvider(context.getBean(DatabaseBackedPagingProvider.class));

		/*
		 * Enable CORS
		 */
        CorsConfiguration config = new CorsConfiguration();
        CorsInterceptor corsInterceptor = new CorsInterceptor(config);
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Prefer");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Access-Control-Request-Method");
        config.addAllowedHeader("Access-Control-Request-Headers");
        config.addAllowedOrigin("*");
        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        registerInterceptor(corsInterceptor);

		/*
		 * Load interceptors for the server from Spring (these are defined in dermal.FhirServerConfig.java)
		 */
        Collection<IServerInterceptor> interceptorBeans = context.getBeansOfType(IServerInterceptor.class).values();
        for (IServerInterceptor interceptor : interceptorBeans) {
            this.registerInterceptor(interceptor);
        }

		/*
		 * If you are hosting this server at a specific DNS name, the server will try to
		 * figure out the FHIR base URL based on what the web container tells it, but
		 * this doesn't always work. If you are setting links in your search bundles that
		 * just refer to "localhost", you might want to use a server address strategy:
		 */
        //setServerAddressStrategy(new HardcodedServerAddressStrategy("http://mydomain.com/fhir/baseDstu2"));

		/*
		 * If you are using DSTU3+, you may want to add a terminology uploader, which allows
		 * uploading of external terminologies such as Snomed CT. Note that this uploader
		 * does not have any security attached (any anonymous user may use it by default)
		 * so it is a potential security vulnerability. Consider using an AuthorizationInterceptor
		 * with this feature.
		 */
        //if (fhirVersion == FhirVersionEnum.DSTU3) {
        //	 registerProvider(myAppCtx.getBean(TerminologyUploaderProviderDstu3.class));
        //}
    }
}

