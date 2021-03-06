package uk.ac.cam.eeci.energyagents.test;

import org.junit.Before;

import java.time.*;
import java.util.Random;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import uk.ac.cam.eeci.energyagents.HeterogeneousMarkovChain;
import uk.ac.cam.eeci.energyagents.Person;
import uk.ac.cam.eeci.energyagents.DwellingReference;
import uk.ac.cam.eeci.energyagents.Person.Activity;

public class TestPerson {

    private static final Duration TIME_STEP_SIZE = Duration.ofMinutes(10);
    private static final Activity INITIAL_ACTIVITY = Activity.NOT_AT_HOME;
    private static final ZonedDateTime INITIAL_DATETIME = ZonedDateTime.of(2017, 02, 11, 16, 20, 0, 0, ZoneOffset.UTC);
    private static final double ACTIVE_METABOLIC_RATE = 200;
    private static final double PASSIVE_METABOLIC_RATE = 50;
    private HeterogeneousMarkovChain<Activity> markovChain = mock(HeterogeneousMarkovChain.class);
    private Random randomNumberGenerator = mock(Random.class);
    private DwellingReference home = mock(DwellingReference.class);
    private Person person;

    @Before
    public void setUp() {
        this.person = new Person(this.markovChain, ACTIVE_METABOLIC_RATE, PASSIVE_METABOLIC_RATE,
                INITIAL_ACTIVITY, INITIAL_DATETIME,
                TIME_STEP_SIZE, home, randomNumberGenerator);
    }

    @Test
    public void testStartsUpWithInitialActivity() {
        assertThat(person.getCurrentActivity(), is(equalTo(INITIAL_ACTIVITY)));
    }

    @Test
    public void testUpdatesStateAccordingToMarkovChainDuringStep() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.SLEEP_AT_HOME);
        person.step();
        assertThat(person.getCurrentActivity(), is(equalTo(Activity.SLEEP_AT_HOME)));
    }

    @Test
    public void testUpdatesTimeDuringStep() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.SLEEP_AT_HOME);
        person.step();
        reset(this.markovChain);
        person.step();
        verify(this.markovChain)
                .move(Activity.SLEEP_AT_HOME, INITIAL_DATETIME.plus(TIME_STEP_SIZE), this.randomNumberGenerator);
    }

    @Test
    public void testEntersHomeWhenStartingBeingAtHome() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.HOME);
        person.step();
        verify(this.home).enter(any());
    }

    @Test
    public void testDoesNotEnterHomeTwice() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.HOME);
        person.step();
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.SLEEP_AT_HOME);
        person.step();
        verify(this.home, times(1)).enter(any());
    }

    @Test
    public void testLeavesHomeWhenStartingToNotBeThere() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.HOME);
        person.step();
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.NOT_AT_HOME);
        person.step();
        verify(this.home).leave(any());
    }

    @Test
    public void testDoesNotLeaveHomeTwice() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.HOME);
        person.step();
        when(this.markovChain.move(Activity.HOME, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.NOT_AT_HOME);
        person.step();
        when(this.markovChain.move(Activity.NOT_AT_HOME, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.NOT_AT_HOME);
        person.step();
        verify(this.home, times(1)).leave(any());
    }

    @Test
    public void returnsActiveMetabolicRateWhenActiveAtHome() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.HOME);
        person.step();
        assertThat(this.person.getCurrentMetabolicRate(), is(equalTo(ACTIVE_METABOLIC_RATE)));
    }

    @Test
    public void returnsActiveMetabolicRateWhenOutside() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.NOT_AT_HOME);
        person.step();
        assertThat(this.person.getCurrentMetabolicRate(), is(equalTo(ACTIVE_METABOLIC_RATE)));
    }

    @Test
    public void returnsPassiveMetabolicRateWhenAsleep() {
        when(this.markovChain.move(INITIAL_ACTIVITY, INITIAL_DATETIME, this.randomNumberGenerator))
                .thenReturn(Activity.SLEEP_AT_HOME);
        person.step();
        assertThat(this.person.getCurrentMetabolicRate(), is(equalTo(PASSIVE_METABOLIC_RATE)));
    }

}
