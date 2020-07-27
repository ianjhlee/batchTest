package net.movill.batch.demo.job;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.movill.batch.demo.entity.ParkingRegister;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class AmanoCarNumberInsertSyncConfiguration {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource; // DataSource DI

  public static final String JOB_NAME = "amanoCarNumberInsertSyncBatch";
  public static final String BEAN_PREFIX = JOB_NAME + "_";

  @Value("${chunkSize:1000}")
  private int chunkSize;

  @Bean(JOB_NAME)
  public Job job() throws Exception {
    return jobBuilderFactory.get(JOB_NAME)
        .preventRestart()
        .start(step())
        .build();
  }

  @Bean(BEAN_PREFIX + "step")
  @JobScope
  public Step step() throws Exception {
    return stepBuilderFactory.get(BEAN_PREFIX + "step")
        .<ParkingRegister, ParkingRegister>chunk(chunkSize)
        .reader(reader())
//        .processor(processor())
        .writer(writer())
        .build();
  }

  @Bean(name = JOB_NAME +"_reader")
  @StepScope
  public JdbcPagingItemReader<ParkingRegister> reader() throws Exception {

    SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
    queryProvider.setDataSource(dataSource);
    queryProvider.setSelectClause("amano.idx, amano.apt_idx, amano.dong, amano.ho, amano.car_number, amano.car_name, amano.car_owner_name, amano.car_owner_contact, amano.status, amano.memo, amano.reg_date, amano.mod_date");
    queryProvider.setFromClause("from amano_parking_register amano left outer join parking_register movill on amano.apt_idx = movill.apt_idx and amano.dong = amano.dong and amano.car_number = movill.car_number");
    queryProvider.setWhereClause("movill.idx is null");

    Map<String, Order> sortKeys = new HashMap<>(1);
    sortKeys.put("amano.idx", Order.ASCENDING);

    queryProvider.setSortKeys(sortKeys);

    return new JdbcPagingItemReaderBuilder<ParkingRegister>()
        .pageSize(chunkSize)
        .fetchSize(chunkSize)
        .dataSource(dataSource)
        .rowMapper(new BeanPropertyRowMapper<>(ParkingRegister.class))
        .queryProvider(queryProvider.getObject())
        .name(BEAN_PREFIX+"reader")
        .build();
  }
//  public ItemProcessor<ParkingRegister, ParkingRegister> processor() {
//    return null;
//  }

//  private ItemWriter<ParkingRegister> writer() {
//    return list -> {
//      for (ParkingRegister parkingRegister : list) {
//        log.info("Current parkingRegister={}", parkingRegister.toString());
//      }
//    };
//  }


  @Bean
  public ItemWriter<ParkingRegister> writer() {
    return new JdbcBatchItemWriterBuilder<ParkingRegister>()
        .dataSource(dataSource)
        .sql("insert into parking_register(apt_idx, dong, ho, car_number, car_name, car_owner_name, car_owner_contact, status, memo, reg_date, mod_date) values (:aptIdx, :dong, :ho, :carNumber, :carName, :carOwnerName, :carOwnerContact, :status, :memo, :regDate, :modDate)")
        .beanMapped()
        .build();
  }

  @Bean
  public PagingQueryProvider createQueryProvider() throws Exception {
    SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
    queryProvider.setDataSource(dataSource); // Database에 맞는 PagingQueryProvider를 선택하기 위해
    queryProvider.setSelectClause("amano.idx, amano.apt_idx, amano.dong, amano.ho, amano.car_number, amano.car_name, amano.car_owner_name, amano.car_owner_contact, amano.status, amano.memo, amano.reg_date, amano.mod_date");
    queryProvider.setFromClause("from amano_parking_register amano left outer join parking_register movill on amano.apt_idx = movill.apt_idx and amano.dong = amano.dong and amano.car_number = movill.car_number");
    queryProvider.setWhereClause("movill.idx is null");

    Map<String, Order> sortKeys = new HashMap<>(1);
    sortKeys.put("amano.idx", Order.ASCENDING);

    queryProvider.setSortKeys(sortKeys);

    return queryProvider.getObject();
  }
}
