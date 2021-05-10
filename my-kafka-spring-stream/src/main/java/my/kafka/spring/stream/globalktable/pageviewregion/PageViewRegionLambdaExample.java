package my.kafka.spring.stream.globalktable.pageviewregion;

import my.kafka.bank.Topic;
import my.kafka.spring.stream.globalktable.GlobalKTablesExample;
import my.kafka.spring.stream.message.PageView;
import my.kafka.spring.stream.message.UserProfile;
import my.kafka.spring.stream.message.ViewRegion;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
public class PageViewRegionLambdaExample {

    public static final Logger logger = LoggerFactory.getLogger(GlobalKTablesExample.class);

    /**
     * Input
     * send data like "userA is viewing pageA"
     * userA is related to one Region
     * output
     * how many views by each region
     *
     * for exmple:
     * Jason (region Asia) is viewing index page
     * ZhangSan (region Asia) is viewing index page
     * Chirs (region north America) is viewing index page
     * the output is
     * Asia: 2
     * North America: 1
     * @param streamsBuilder
     * @return
     */
    @Bean
    public KStream<String, Long> pageViewRegionStream(StreamsBuilder streamsBuilder) {
        Serde<PageView> pageViewSerde = new JsonSerde<>(PageView.class);
        final KStream<String, PageView> views = streamsBuilder.stream(Topic.PAGE_VIEWS,
                Consumed.with(Serdes.String(), pageViewSerde))
                .map((k,v) -> KeyValue.pair(v.getUserId(), v));

        Serde<UserProfile> userProfileSerde = new JsonSerde<>(UserProfile.class);
        final KTable<String, UserProfile> userProfiles = streamsBuilder.table(Topic.USER_PROFILES,
                Consumed.with(Serdes.String(), userProfileSerde));

        final KTable<String, String> userRegions = userProfiles.mapValues(UserProfile::getRegion);

        KStream<String, ViewRegion> viewRegionKStream = views
                .leftJoin(userRegions,
                        (view, region) -> {
                            final ViewRegion viewRegion = ViewRegion.builder()
                                    .view(view.getPage())
                                    .region(region)
                                    .build();
                            return viewRegion;
                        },
                        Joined.with( // For the joined result, serdes is not same with default config from yml
                                Serdes.String(), /* key serdes */
                                pageViewSerde,   /* left value serdes */
                                Serdes.String()  /* right value serdes */
                        ));

        KGroupedStream<String, ViewRegion> viewRegionKGroupedStream = viewRegionKStream
                .map((user, viewRegion) -> new KeyValue<>(viewRegion.getRegion(), viewRegion))
//                .peek((k, v) -> System.out.println("mapped region view: " + k + " | " + v))
        .groupBy((user, viewRegion) -> user,
                /* specify grouped serdes */
                /* the error "class java.lang.String cannot be cast to class" is mainly caused by incorrect serdes */
                Grouped.with(Serdes.String(), new JsonSerde<ViewRegion>(ViewRegion.class))
        );

        final KTable<Windowed<String>, Long> viewsByRegion = viewRegionKGroupedStream
                // count views by region, using hopping windows of size 2 minutes that advance every 1 minute
                .windowedBy(TimeWindows.of(Duration.ofMinutes(2)).advanceBy(Duration.ofMinutes(1)))
                .count();

        final KStream<String, Long> viewsByRegionStream = viewsByRegion
                .toStream((windowedRegion, count) -> windowedRegion.toString());

        viewsByRegionStream.to(Topic.PAGE_VIEWS_BY_REGION, Produced.with(Serdes.String(), Serdes.Long()));

        return viewsByRegionStream;
    }

}
