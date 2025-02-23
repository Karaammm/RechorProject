package ch.epfl.rechor;

import static org.junit.Assert.*;

import org.junit.jupiter.api.Test;

import ch.epfl.rechor.IcalBuilder.Name;

public class MyIcalBuilderTest {

    @Test
    void addValueWorksForShortString() {
        IcalBuilder builderboy = new IcalBuilder();
        builderboy.add(Name.BEGIN, "justTesting");
        assertEquals("BEGIN:justTesting\r\n", builderboy.build());
    }

    @Test
    void addValueWorksForLongString() {
        IcalBuilder builderboy = new IcalBuilder();
        builderboy.add(Name.DESCRIPTION,
                "This string is here just to make sure it is over 75 characters long lalalala");
        assertEquals(
                "DESCRIPTION:This string is here just to make sure it is over 75 characters \r\n long lalalala\r\n",
                builderboy.build());
    }
}
