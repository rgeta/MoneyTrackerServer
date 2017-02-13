import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MoneyTrackerServer extends Application {

	// Основное окно
	public Stage primaryStage;

	// База данных
	// public DB db;

	// Флаг для отслеживания завершения сервера
	public boolean mServerWorking;

	// Область ведения лога
	public TextArea textArea;

	// Идентификаторы пунктов меню
	public static final String ID_START = "ID_START";
	public static final String ID_STOP = "ID_STOP";
	public static final String ID_EXIT = "ID_EXIT";

	// Серверная "нить"
	ServerThread serverThread;

	public static void main(String[] args) {
		Application.launch(args);
	}

	private MenuBar buildMenuBarWithMenus(
			final ReadOnlyDoubleProperty menuWidthProperty) {
		final MenuBar menuBar = new MenuBar();

		// Prepare left-most 'File' drop-down menu
		final Menu fileMenu = new Menu("File");

		// Пункт меню подключения
		final MenuItem miStart = new MenuItem("Start server");
		miStart.setId(MoneyTrackerServer.ID_START);
		fileMenu.getItems().add(miStart);

		// Пункт меню отключения
		final MenuItem miStop = new MenuItem("Stop server");
		miStop.setId(MoneyTrackerServer.ID_STOP);
		miStop.setDisable(true);
		fileMenu.getItems().add(miStop);

		// Разделитель
		fileMenu.getItems().add(new SeparatorMenuItem());

		// Пункт меню отключения
		final MenuItem miExit = new MenuItem("Exit");
		miExit.setId(MoneyTrackerServer.ID_EXIT);
		miExit.setAccelerator(new KeyCodeCombination(KeyCode.X,
				KeyCombination.CONTROL_DOWN));
		fileMenu.getItems().add(miExit);

		// Добавляем обработчик событий для меню
		fileMenu.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				String itemId = ((MenuItem) e.getTarget()).getId();
				if (itemId.equals(ID_START)) {

					// Запрещаем кнопку запуска сервера
					miStart.setDisable(true);
					try {

						// Запускаем сервер
						serverThread = new ServerThread(textArea);
						serverThread.start();
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {

						// Разрешаем кнопку остановки сервера
						miStop.setDisable(false);
					}
				} else if (itemId.equals(ID_STOP)) {

					// Запрещаем кнопку остановки сервера
					miStop.setDisable(true);
					try {

						// Отдаем команду закрытия сокета
						serverThread.closeSocket();
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {

						// Разрешаем кнопку запуска сервера
						miStart.setDisable(false);
					}

					// Ставим флаг завершения сервера
					serverThread.interrupt();
				} else if (itemId.equals(ID_EXIT)) {
					stop();
				}
			}

		});

		// Добавляем подменю
		menuBar.getMenus().add(fileMenu);

		// Prepare 'Help' drop-down menu
		final Menu helpMenu = new Menu("Help");

		final MenuItem onlineManualMenuItem = new MenuItem("Server manual");
		helpMenu.getItems().add(onlineManualMenuItem);
		helpMenu.getItems().add(new SeparatorMenuItem());

		final MenuItem aboutMenuItem = new MenuItem("About");
		aboutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				StageAbout stageAbout = new StageAbout(primaryStage);
				stageAbout.show();
			}
		});
		aboutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A,
				KeyCombination.CONTROL_DOWN));
		helpMenu.getItems().add(aboutMenuItem);
		menuBar.getMenus().add(helpMenu);

		// bind width of menu bar to width of associated stage
		menuBar.prefWidthProperty().bind(menuWidthProperty);

		return menuBar;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		final MenuBar menuBar = buildMenuBarWithMenus(primaryStage
				.widthProperty());

		VBox root = new VBox();

		// Текстовая область
		textArea = new TextArea();
		// textArea.setMaxHeight(Double.MAX_VALUE);
		textArea.setEditable(false);
		textArea.setFocusTraversable(false);

		root.getChildren().add(menuBar);
		VBox vBoxForText = new VBox();
		// vBoxForText.getChildren().add(new Button("Test"));

		Label label = new Label("Server log area:");
		label.setPadding(new Insets(0, 0, 8, 0));
		vBoxForText.getChildren().add(label);
		vBoxForText.setPadding(new Insets(8, 8, 8, 8));
		vBoxForText.getChildren().add(textArea);
		root.getChildren().add(vBoxForText);

		Scene scene = new Scene(root, 800, 600);

		// Привязка размеров области к размерам окна
		textArea.prefHeightProperty().bind(scene.heightProperty());
		textArea.prefWidthProperty().bind(scene.widthProperty());

		primaryStage.setTitle("MoneyTrackerServer 2.0");
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(300);
		primaryStage.setMinWidth(400);
		this.primaryStage = primaryStage;

		primaryStage.getIcons().add(
				new Image(MoneyTrackerServer.class
						.getResourceAsStream("/images/icon16x16.png")));

		// Отображаем окно
		primaryStage.show();

		// Подключаемся к базе данных
		// this.db = new DB();
		// db.Connect();
		// db.Query("PRAGMA ENCODING = \"UTF-8\"");
		// ResultSet resultset = db.executeQuery("SELECT * FROM TCheckList");
		// System.out.println(resultset.getString("CheckListName"));
		// System.out.println("Тест");
	}

	@Override
	public void stop() {
		try {

			// Отдаем команду закрытия сокета
			serverThread.closeSocket();
			
			// Ставим флаг завершения сервера
			serverThread.interrupt();
		} catch (Exception e1) {
		} finally {
			primaryStage.close();
		}
	}
}
