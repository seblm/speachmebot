package speachmebot;

import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

public class SpeachMeBotTest {

    @Test
    public void should_find_next_wednesday_when_monday() {
        Clock monday = Clock.fixed(Instant.parse("2016-11-21T13:15:30.00Z"), ZoneId.of("UTC"));

        LocalDate nextWednesday = SpeachMeBot.nextDayOfWeek(monday, WEDNESDAY);

        assertThat(nextWednesday).isEqualTo("2016-11-23");
    }

    @Test
    public void should_find_next_wednesday_when_friday() {
        Clock friday = Clock.fixed(Instant.parse("2016-11-25T13:15:30.00Z"), ZoneId.of("UTC"));

        LocalDate nextWednesday = SpeachMeBot.nextDayOfWeek(friday, WEDNESDAY);

        assertThat(nextWednesday).isEqualTo("2016-11-30");
    }

}
