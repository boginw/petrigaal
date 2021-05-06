package petrigaal.app;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import petrigaal.strategy.TopDownStrategySynthesiser.SynthesisState;

public class SynthesisStateListView extends ListView<SynthesisState> {
    public SynthesisStateListView(ObservableList<SynthesisState> items) {
        super(items);

        setCellFactory(new Callback<>() {
            @Override
            public ListCell<SynthesisState> call(ListView<SynthesisState> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(SynthesisState item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(String.valueOf(item.close()));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
    }
}
