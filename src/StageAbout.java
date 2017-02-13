import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StageAbout extends Stage {
	
	StageAbout(Stage parentStage) {
		super();
		initStyle(StageStyle.UTILITY);
		initModality(Modality.WINDOW_MODAL);
		initOwner(parentStage);
		setResizable(false);
		setTitle("About");
		
		// Разместим поля с помощью GridPane
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(16, 16, 16, 16));
		 
		Scene scene = new Scene(grid, 300, 180);
		setScene(scene);
		
		Label lblAbout = new Label("Use MoneyTrackerServer if you want to backup or restore Android MoneyTracker data. "
					+ "This program is experimental so please be careful! It may contain some hidden errors."
				+ "\n\n\u00A9 2017 Rostislav Geta");
		lblAbout.setWrapText(true);
		grid.add(lblAbout, 0, 0);
		 
		Button btnOK = new Button("OK");
		grid.add(btnOK, 0, 1);
		
		// Обработчик нажатий кнопки
		btnOK.setOnAction(new EventHandler<ActionEvent>() {
			 
		    @Override
		    public void handle(ActionEvent e) {
		    	close();
		    }

		});		
	}

}