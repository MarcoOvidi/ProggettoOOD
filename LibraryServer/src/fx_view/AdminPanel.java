package fx_view;

import java.text.ParseException;

import controller.AdministrationController;
import controller.EditUserController;
import controller.LocalSession;
import dao.DatabaseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import model.User;
import vo.UUIDUser;
import vo.UserRow;

public class AdminPanel {

	@FXML 
	private TableView<UserRow> users;
	@FXML
	private ObservableList<UserRow> rows;
	@FXML 
	private	TableColumn<UserRow, UUIDUser> userID;
	@FXML 
	private	TableColumn<UserRow, String> username;
	@FXML 
	private	TableColumn<UserRow, String> name;
	@FXML 
	private	TableColumn<UserRow, String> surname;
	@FXML 
	private	TableColumn<UserRow, String> email;


	/*@FXML
	private Button done;*/

	@FXML
	public void initialize() throws DatabaseException, ParseException {
		initUserList();
		initRowClick();
	}
	
	private void initUserList() {
		userID.setCellValueFactory(new PropertyValueFactory<UserRow, UUIDUser>("userID"));
		username.setCellValueFactory(new PropertyValueFactory<UserRow, String>("username"));
		name.setCellValueFactory(new PropertyValueFactory<UserRow, String>("name"));
		surname.setCellValueFactory(new PropertyValueFactory<UserRow, String>("surname"));
		email.setCellValueFactory(new PropertyValueFactory<UserRow, String>("email"));
		userID.setVisible(false);
		
		rows = FXCollections.observableArrayList();
		
		for (User user : AdministrationController.loadUserList()) {
			UserRow row = new UserRow(user.getID(),user.getUsername(),user.getInformations().getName(),user.getInformations().getSurname(),user.getInformations().getMail());
			rows.add(row);
		}
		
		users.setItems(rows);
	}
	
	public void initRowClick() {
		users.setRowFactory(tv -> {
			TableRow<UserRow> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					/*clickedDocument = row.getItem();
					loadClickedDocumentProjects();*/
					EditUserController.setEditingUser(LocalSession.getLocalUser().getID());
					EditUserController.setToEditUser(row.getItem().getId());
					EditUserController.startEditing();					
				}
			});
			return row;
		});
	}


}
