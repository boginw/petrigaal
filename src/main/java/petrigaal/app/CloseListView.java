package petrigaal.app;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import petrigaal.edg.DGConfiguration;

import java.util.Map;

public class CloseListView extends ListView<Map<DGConfiguration, Boolean>> {
    public CloseListView(ObservableList<Map<DGConfiguration, Boolean>> items) {
        super(items);

        setCellFactory(new Callback<>() {
            @Override
            public ListCell<Map<DGConfiguration, Boolean>> call(ListView<Map<DGConfiguration, Boolean>> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Map<DGConfiguration, Boolean> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(String.valueOf(item.keySet()));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
    }
}
