package utils;

import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.view.View;
import com.serotonin.mango.view.component.CompoundChild;
import com.serotonin.mango.view.component.SimpleCompoundComponent;
import com.serotonin.mango.view.component.SimplePointComponent;
import com.serotonin.mango.view.component.ViewComponent;
import com.serotonin.mango.vo.DataPointVO;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ViewTestUtils {

    private static final AtomicInteger i = new AtomicInteger();

    public static View newView(DataPointVO... dataPoints) {

        List<ViewComponent> viewComponents = collectViewComponents(dataPoints);

        View view = new View();
        viewComponents.forEach(view::addViewComponent);
        view.setId(i.incrementAndGet());
        return view;
    }

    private static List<ViewComponent> collectViewComponents(DataPointVO... dataPoints) {
        return Stream.of(dataPoints)
                    .limit(dataPoints.length/2)
                    .map(a -> {
                        SimplePointComponent pointComponent =
                                (SimplePointComponent) ViewComponent
                                        .newInstance(SimplePointComponent.DEFINITION.getName());
                        pointComponent.tsetDataPoint(a);
                        if(i.incrementAndGet() % 2 == 0) {
                            return pointComponent;
                        } else {
                            SimpleCompoundComponent simpleCompoundComponent = (SimpleCompoundComponent) ViewComponent.newInstance(SimpleCompoundComponent.DEFINITION.getName());
                            simpleCompoundComponent.getChildComponents().add(new CompoundChild(String.valueOf(i.incrementAndGet()),null, pointComponent, new int[]{}));
                            return simpleCompoundComponent;
                        }
                    })
                    .collect(Collectors.toList());
    }


    public static View newView(List<DataPointVO> dataPoints, List<Integer> ids, ShareUser shareUser) {

        DataPointVO[] dataPoints1 = dataPoints.stream()
                .filter(a -> ids.contains(a.getId()))
                .toArray(DataPointVO[]::new);

        List<ViewComponent> viewComponents = collectViewComponents(dataPoints1);

        View view = new View();
        viewComponents.forEach(view::addViewComponent);
        view.setId(i.incrementAndGet());
        view.setViewUsers(Arrays.asList(shareUser));
        return view;
    }
}
