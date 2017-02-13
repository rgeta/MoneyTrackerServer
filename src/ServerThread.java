import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextArea;

public class ServerThread extends Thread {

	// Сам сокет
	private ServerSocket serverSocket;

	// Хранение ссылки на лог
	private TextArea textArea;
	private Socket socket;
	
	// IP-адрес сервера
	InetAddress address = InetAddress.getLocalHost();

	// Класс для обозначения этапов обмена данными клиента и сервера
	private enum Stage {
		START, AWAITING_COMMAND, AWAITING_DATA, STOP
	};
	
	// Проверяет имя файла на корректность
	public static boolean isFileNameCorrect(String fileName) {
		Pattern pattern = Pattern.compile("(.+)?[><\\|\\?*/:\\\\\"](.+)?");
		Matcher matcher = pattern.matcher(fileName);
		return !matcher.find();
	}

	public ServerThread(TextArea _textArea) throws IOException {
		textArea = _textArea;
	}
	
	public void run() {
		//textArea.appendText("\nDevice connected");
		
		try {
			serverSocket = new ServerSocket(8888);
			if (!textArea.getText().isEmpty()) {
				textArea.appendText("\n--------------------\n");
			}
			textArea.appendText("Server started\nIP - " + address + "\nPort - " + serverSocket.getLocalPort());
			
			// Потоки для записи и чтения файла с данными
			FileOutputStream foutput = null;
			FileInputStream finput = null;

			// Имя файла, который будет создан при бэкапе или экспорте
			String fileName = null;

			// Команда LOAD или SAVE
			String direction = null;

			// Идентификатор подключенного устройства
			String id = null;
			
			// Цикл работы сервера
			while (!isInterrupted()) {
				Socket server = serverSocket.accept();
				textArea.appendText("\nDevice connected");
				
				// Читать/писать будем байты (чтобы передавались двоичные данные
				// без влияния кодировок)

				DataInputStream input = new DataInputStream(server.getInputStream());
				DataOutputStream output = new DataOutputStream(server.getOutputStream());

				// Предельное количество байт буфера
				int limit = 2048;

				// Буфер для передачи данных
				byte[] buffer = new byte[limit];

				// Этап работы сервера
				Stage stage = Stage.START;

				// Класс для буферизованного чтения (реализовал, чтобы читать
				// команды по TELNET для тестовых целей)
				BufferedReader bufferedInput = new BufferedReader(new InputStreamReader(input));

				// Основной цикл работы сервера
				while (true) {

					// Отправляем клиенту приветственное сообщение
					if (stage == Stage.START) {
						output.writeUTF("Welcome to MoneyTrackerServer!\r");
						textArea.appendText("\nWelcome to MoneyTrackerServer!");
						stage = Stage.AWAITING_COMMAND;
					}

					if (stage == Stage.AWAITING_COMMAND) {
						String command = bufferedInput.readLine();
						if ((command == null) || (command.startsWith("QUIT"))) {
							stage = Stage.STOP;
							break;
						}
						if (command.startsWith("LOAD")) {

							// Загрузка файла на устройство
							// Сначала проверим полноту данных
							if ((id.isEmpty()) || (fileName.isEmpty())) {
								textArea.appendText("\n-ERR Could't start loading");
								output.writeUTF("-ERR Could't start loading\r");
							} else {
								// Пытаемся открыть файл на чтение
								File file = new File("c://data//" + id + "//" + fileName);

								// Создаем поток для этого файла
								finput = new FileInputStream(file);
								stage = Stage.AWAITING_DATA;
								direction = "LOAD";
								output.writeLong(file.length());
							}
						} else if (command.startsWith("SAVE")) {

							// Сохранение файла на сервер
							// Сначала проверим полноту данных
							if ((id.isEmpty()) || (fileName.isEmpty())) {
								textArea.appendText("\n-ERR Could't start saving");
								output.writeUTF("-ERR Could't start saving\r");
							} else {
								stage = Stage.AWAITING_DATA;

								// Создаем файл для последующей потоковой записи
								File file = new File("c://data//" + id + "//" + fileName);

								// Создаем поток для этого файла
								foutput = new FileOutputStream(file);
								direction = "SAVE";
							}
						} else if (command.startsWith("FILE ")) {

							// Выделяем переданное клиентом имя файла
							fileName = command.substring(5).trim();
							if (isFileNameCorrect(fileName)) {
								textArea.appendText("\n+OK I have got the file name = " + fileName);
								output.writeUTF("+OK I have got the file name\r");
							} else {
								textArea.appendText("\n-ERR Bad file name");
								output.writeUTF("-ERR Bad file name\r");
								fileName = null;
							}
						} else if (command.startsWith("DEVICE ")) {

							// Выделяем переданный клиентом ID
							id = command.substring(7).trim();

							// Проверяем существование папки с заданным ID
							File dataFolder = new File("c://data//" + id);
							if (!dataFolder.exists()) {

								// Создаем папку
								dataFolder.mkdir();
							}
							if (dataFolder.exists()) {
								textArea.appendText("\n+OK I have got the device ID = " + id);
								output.writeUTF("+OK I have got the device ID\r");
							} else {
								textArea.appendText("\n-ERR Bad ID");
								output.writeUTF("-ERR Bad ID\r");
								id = null;
							}
						} else {
							output.writeUTF("-ERR Unknown command\r");
							textArea.appendText("\n" + command);
							textArea.appendText("\n-ERR Unknown command");
						}
					} else if (stage == Stage.AWAITING_DATA) {
						if (direction.equals("SAVE")) {

							// Получаем от клиента любые данные
							int c = input.read(buffer, 0, limit);
							if (c == -1) {
								stage = Stage.STOP;
							} else {

								// Записываем полученные от клиента данные в
								// выходной файловый поток
								foutput.write(buffer, 0, c);
							}
						} else if (direction.equals("LOAD")) {
							int c = finput.read(buffer, 0, limit);
							if (c == -1) {
								stage = Stage.STOP;
							} else {

								// Записываем полученные от клиента данные в
								// выходной файловый поток
								output.write(buffer, 0, c);
							}
						}
					} else if (stage == Stage.STOP) {
						// Закрываем потоки
						if (foutput != null)
							foutput.close();
						if (output != null)
							output.close();
						if (finput != null)
							finput.close();
						if (input != null)
							input.close();
						textArea.appendText("\n+OK Transfer completed");
						textArea.appendText("\n--------------------");

						// Закрываем соединение с клиентом
						server.close();
						break;
					}
				}
	        }
		} catch (IOException e) {
				//textArea.appendText("\n" + e.getMessage());
		} finally {
			try {
				if (socket != null)
					socket.close();
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			textArea.appendText("\nServer stopped");
		}
	}
	
	public void closeSocket() throws IOException
	{
		if ((socket != null) && (!socket.isClosed()))
			socket.close();
		serverSocket.close();
	}
	
	public void finish() throws IOException
	{
	}	

}
