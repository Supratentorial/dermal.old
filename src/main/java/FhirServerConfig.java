import ca.uhn.fhir.jpa.config.BaseJavaConfigDstu3;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Created by blake on 19/05/2017.
 */
public class FhirServerConfig extends BaseJavaConfigDstu3 {

    @Bean()
    public DaoConfig daoConfig(){
        DaoConfig daoConfig = new DaoConfig();
        daoConfig.setSubscriptionEnabled(true);
        daoConfig.setSubscriptionPollDelay(5000);
        daoConfig.setAllowMultipleDelete(true);
        return daoConfig;
    }

    @Bean(destroyMethod = "close")
    public DataSource dataSource(){
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://203.170.84.193:3306/indoctri_dermal");
        dataSource.setUsername("indoctri_admin");
        dataSource.setPassword("oQqrMk7rD6O5");
        return dataSource;
    }

}
