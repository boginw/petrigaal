package petrigaal.app;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import petrigaal.edg.Configuration;

import java.util.Map;

public class CloseListView extends ListView<Map<Configuration, Boolean>> {
    public CloseListView(ObservableList<Map<Configuration, Boolean>> items) {
        super(items);

        setCellFactory(new Callback<>() {
            @Override
            public ListCell<Map<Configuration, Boolean>> call(ListView<Map<Configuration, Boolean>> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Map<Configuration, Boolean> item, boolean empty) {
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
