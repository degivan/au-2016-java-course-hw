import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static spbau.mit.divan.practice.collectors.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        assertEquals(
                Arrays.asList("data/input1.txt", "data/input3.txt"),
                findQuotes(Arrays.asList("data/input1.txt", "data/input2.txt"
                        , "data/input3.txt", "data/input4.txt"), "true"));
    }

    private static final double EPS = 0.01;

    @Test
    public void testPiDividedBy4() {
        final double res1 = Math.PI / 4;
        assertEquals(res1, piDividedBy4(), EPS);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> data = new HashMap<>();
        data.put("Burroughs", Arrays.asList("A", "B", "C"));
        data.put("Kerouac", Arrays.asList("D", "E", "F", "G"));
        data.put("Tolstoy", Arrays.asList("Всё смешалось в доме Облонских. Жена узнала, ",
                "что муж был в связи с бывшею в их доме француженкою-гувернанткой, ",
                "и объявила мужу, что не может жить с ним в одном доме"));
        assertEquals("Tolstoy", findPrinter(data));
    }

    @Test
    public void testCalculateGlobalOrder() {
        List<Map<String, Integer>> data = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Integer> market = new HashMap<>();
            market.put("Apple", i + 1);
            market.put("Watermelon", i + 2);
            if (i == 2) {
                market.put("Bottle", 30);
            }
            data.add(market);
        }
        Map<String, Integer> result = calculateGlobalOrder(data);
        assertEquals(6, (long) result.get("Apple"));
        assertEquals(9, (long) result.get("Watermelon"));
        assertEquals(30, (long) result.get("Bottle"));
    }
}
