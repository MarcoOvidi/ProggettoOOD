package fx_view.controller;

import java.text.ParseException;

import controller.AdministrationController;
import controller.EditUserController;
import controller.LocalSessionBridge;
import dao.concrete.DatabaseException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import vo.UserInformations;
import vo.UserPermissions;

public class UserProfile {

	@FXML
	private Label username;
	@FXML
	private Label name;
	@FXML
	private Label surname;
	@FXML
	private Label email;
	@FXML
	private Label level;
	@FXML
	private Label levelT;
	@FXML
	private CheckBox active;
	@FXML
	private CheckBox download;
	@FXML
	private CheckBox publishDocument;
	@FXML
	private CheckBox editMetaData;
	@FXML
	private CheckBox addNewProject;
	@FXML
	private CheckBox assignDigitalizationTask;
	@FXML
	private CheckBox upload;
	@FXML
	private CheckBox requestTranscriptionTask;
	@FXML
	private CheckBox assignTranscriptionTask;
	@FXML
	private CheckBox modifyTranscription;
	@FXML
	private CheckBox reviewPage;
	@FXML
	private CheckBox reviewTranscription;

	@FXML
	private Button edit;

	@FXML
	public void initialize() throws DatabaseException, ParseException {
		loadInfo();
		loadPermissions();
		initEditButton();
	}

	private void loadInfo() {
		UserInformations info = LocalSessionBridge.getLocalUserInfo();
		username.setText(LocalSessionBridge.getLocalUser().getUsername());
		name.setText(info.getName());
		surname.setText(info.getSurname());
		email.setText(info.getMail().getEmail());

		if (LocalSessionBridge.getLocalUser().isTranscriber()) {
			level.setText(
					String.valueOf(AdministrationController.getTranscriberLevel(LocalSessionBridge.getLocalUser().getID())));
		}else {
			levelT.setVisible(false);
			level.setVisible(false);
		}
	}

	private void loadPermissions() {
		UserPermissions up = LocalSessionBridge.getLocalUser().getPermissions();
		if (up.getActive())
			active.setSelected(true);
		if (up.getAddNewProjectPerm())
			addNewProject.setSelected(true);
		if (up.getAssignDigitalizationTaskPerm())
			assignDigitalizationTask.setSelected(true);
		if (up.getAssignTranscriptionTaskPerm())
			assignTranscriptionTask.setSelected(true);
		if (up.getDownloadPerm())
			download.setSelected(true);
		if (up.getEditMetaDataPerm())
			editMetaData.setSelected(true);
		if (up.getModifyTranscriptionPerm())
			modifyTranscription.setSelected(true);
		if (up.getPublishDocumentPerm())
			publishDocument.setSelected(true);
		if (up.getRequestTranscriptionTaskPerm())
			requestTranscriptionTask.setSelected(true);
		if (up.getReviewPagePerm())
			reviewPage.setSelected(true);
		if (up.getReviewTranscriptionPerm())
			reviewTranscription.setSelected(true);
		if (up.getUploadPerm())
			upload.setSelected(true);

	}

	private void initEditButton() {
		edit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			EditUserController.setEditingUser(LocalSessionBridge.getLocalUser().getID());
			EditUserController.setToEditUser(LocalSessionBridge.getLocalUser());
			SceneController.loadScene("editUserProfile");
		});
	}

}
