package view;

import java.text.ParseException;

import controller.LoginController;
import dao.DatabaseException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LoginScene {

	private static boolean autoLogin = true;

	String controllerUrl = "LoginController";

	public LoginScene() {
	}

	@FXML
	private Font x31;

	@FXML
	private Color x41;

	@FXML
	private Font x1;

	@FXML
	private Color x2;

	@FXML
	private TextField username;

	@FXML
	private PasswordField password;

	@FXML
	private Button login;

	@FXML
	private Button recover;

	@FXML
	private Button register;

	@FXML
	private Label message;

	@FXML
	void go() {
		String usr = username.getText();
		String psw = password.getText();
		if (usr.equals("") || psw.equals("")) {
			displayMessage("Username and password cannot be blank");
			return;
		}
		try {
			LoginController.login(this, usr, psw);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void loginButtonPushed(MouseEvent event) throws Exception {
		go();
	}

	@FXML
	public void initialize() throws DatabaseException, ParseException {
		if (autoLogin) {
			username.setText("Username");
			password.setText("Password");
			Platform.runLater(() -> go());
			autoLogin = false;
		}

		login.setFocusTraversable(true);
		recover.setFocusTraversable(true);

		username.getParent().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() != KeyCode.SHIFT && event.getCode() != KeyCode.TAB) {
				username.setText(event.getCharacter());
				username.requestFocus();
			}
		});

		username.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			/*
			 * if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
			 * recover.requestFocus(); }
			 */
			if (event.getCode() == KeyCode.ENTER) {
				password.requestFocus();
			} else if (event.getCode() == KeyCode.ESCAPE) {
				username.setText("");
				password.setText("");
				username.getParent().requestFocus();
			}
			if (event.getCode() != KeyCode.TAB && event.getCode() != KeyCode.BACK_SPACE
					&& event.getCode() != KeyCode.DELETE && event.getCode() != KeyCode.RIGHT
					&& event.getCode() != KeyCode.LEFT)
				event.consume();
		});

		password.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				go();
			} else if (event.getCode() == KeyCode.ESCAPE) {
				username.setText("");
				password.setText("");
				username.getParent().requestFocus();
			}
			if (event.getCode() != KeyCode.TAB && event.getCode() != KeyCode.BACK_SPACE
					&& event.getCode() != KeyCode.DELETE && event.getCode() != KeyCode.RIGHT
					&& event.getCode() != KeyCode.LEFT)
				event.consume();
		});

		login.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER)
				go();
		});

		register.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER)
				SceneController.loadScene("registration");
		});

		register.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			SceneController.loadScene("registration");
		});

		Platform.runLater(() -> username.getParent().requestFocus());
	}

	public void displayMessage(String msg) {
		message.setText(msg);
	}

}
