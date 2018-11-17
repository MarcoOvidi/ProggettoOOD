package fx_view;

import java.util.HashMap;
import java.util.Map.Entry;

import controller.PageTranscriptionController;
import controller.ScanningProjectController;
import controller.TranscriptionProjectController;
import dao.DatabaseException;
import dao.ScanningWorkProjectQuerySet;
import dao.TranscriptionWorkProjectQuerySet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import model.Page;
import model.User;
import vo.DocumentRow;
import vo.PageTranscriptionLog;
import vo.StaffRow;
import vo.UUIDDocument;
import vo.UUIDScanningWorkProject;
import vo.UUIDTranscriptionWorkProject;
import vo.UUIDUser;

public class ManageProject {

	@FXML
	private AnchorPane topbar;

	@FXML
	private TableView<DocumentRow> documentTable;

	@FXML
	private TableColumn<DocumentRow, String> Document;

	@FXML
	private TableColumn<DocumentRow, String> Transcription_PRJ;

	@FXML
	private TableColumn<DocumentRow, String> Scanning_PRJ;

	@FXML
	private TabPane pane;

	@FXML
	private Tab transcriptionTab;

	@FXML
	private Tab scanningTab;

	@FXML
	private TabPane transcriptionTabPane;

	@FXML
	private TabPane scanningTabPane;

	@FXML
	private Tab transcriptionPagesTab;

	@FXML
	private Tab scanningPagesTab;

	@FXML
	private Tab transcriptionTranscribersTab;

	@FXML
	private Tab scanningDigitalizersTab;

	@FXML
	private AnchorPane transcriptionPagesAnchor;

	@FXML
	private AnchorPane scanningPagesAnchor;

	@FXML
	private AnchorPane transcriptionTranscriberAnchor;

	@FXML
	private AnchorPane scanningDigitalizersAnchor;

	@FXML
	private ObservableList<DocumentRow> rows;

	@FXML
	private TableView<StaffRow> transcriptionStaff;

	@FXML
	private TableColumn<StaffRow, String> usernameTranscriber;

	@FXML
	private TableColumn<StaffRow, String> roleTranscriber;

	@FXML
	private ObservableList<StaffRow> transcriptionProjectStaff;

	@FXML
	private TableView<PageTranscriptionLog> transcriptionLog;

	@FXML
	private TableColumn<PageTranscriptionLog, String> pageNumber;

	@FXML
	private TableColumn<PageTranscriptionLog, String> transcriptionReviser;

	@FXML
	private TableColumn<PageTranscriptionLog, String> transcriptionRevised;

	@FXML
	private TableColumn<PageTranscriptionLog, String> transcriptionValidated;

	@FXML
	private TableColumn<PageTranscriptionLog, String> transcriptionTranscriber;
	
	@FXML
	private ObservableList<PageTranscriptionLog> listLog;

	public void initialize() {
		try {
			tableDocumentFiller();
			rowClick();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public void tableDocumentFiller() throws NullPointerException, DatabaseException {
		Document.setCellValueFactory(new PropertyValueFactory<DocumentRow, String>("Document"));
		Transcription_PRJ.setCellValueFactory(new PropertyValueFactory<DocumentRow, String>("Transcription_PRJ"));
		Scanning_PRJ.setCellValueFactory(new PropertyValueFactory<DocumentRow, String>("Scanning_PRJ"));

		rows = FXCollections.observableArrayList();

		ScanningProjectController.loadCoordinatedScanningPRoject();
		TranscriptionProjectController.loadCoordinatedTranscriptionPRoject();

		HashMap<UUIDDocument, DocumentRow> document = new HashMap<UUIDDocument, DocumentRow>();

		// TODO credo che tutto ciò vada nel controller
		for (Entry<UUIDScanningWorkProject, String[]> entry : ScanningProjectController.getCoordinatedScanningProject()
				.entrySet()) {
			String[] array = entry.getValue();
			// TODO chiamare tramite controller
			String format = "";
			if (array[1].equalsIgnoreCase("true"))
				format = "\u2714";
			else if (array[1].equals("false"))
				format = "\u2718";
			DocumentRow dr = new DocumentRow(array[0], format, entry.getKey());

			document.put(ScanningWorkProjectQuerySet.loadUUIDDocument(entry.getKey()), dr);

			System.out.println(dr);
		}

		for (Entry<UUIDTranscriptionWorkProject, String[]> entry : TranscriptionProjectController
				.getCoordinatedTranscriptionProject().entrySet()) {
			// TODO chiamare tramite controller
			String[] array = entry.getValue();
			// TODO questo cazzo di if non funziona va sempre nell'else
			if (document.containsKey(TranscriptionWorkProjectQuerySet.loadUUIDDocument(entry.getKey()))) {
				String format = "";
				if (array[1].equalsIgnoreCase("true"))
					format = "\u2714";
				else if (array[1].equals("false"))
					format = "\u2718";

				DocumentRow dr = document.get(TranscriptionWorkProjectQuerySet.loadUUIDDocument(entry.getKey()));
				dr.setTranscription_PRJ(format);
				dr.setIdTPrj(entry.getKey());
				document.put(TranscriptionWorkProjectQuerySet.loadUUIDDocument(entry.getKey()), dr);
			} else {
				String format = "";
				if (array[1].equalsIgnoreCase("true"))
					format = "\u2714";
				else if (array[1].equals("false"))
					format = "\u2718";
				DocumentRow d = new DocumentRow(array[0], format, entry.getKey());
				document.put(TranscriptionWorkProjectQuerySet.loadUUIDDocument(entry.getKey()), d);
			}
		}

		for (DocumentRow docRow : document.values()) {
			rows.add(docRow);
		}

		documentTable.setItems(rows);
	}

	public void rowClick() {
		documentTable.setRowFactory(tv -> {
			TableRow<DocumentRow> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {

					DocumentRow clickedRow = row.getItem();
					loadPagesLog(clickedRow);
					loadTranscriptionProjectStaff(clickedRow);
				}
			});
			return row;
		});

	}

	public void loadPagesLog(DocumentRow dR) {
		
		pageNumber.setCellValueFactory(new PropertyValueFactory<PageTranscriptionLog, String>("pageNumber"));
		transcriptionReviser.setCellValueFactory(new PropertyValueFactory<PageTranscriptionLog, String>("transcriptionReviser"));
		transcriptionRevised.setCellValueFactory(new PropertyValueFactory<PageTranscriptionLog, String>("transcriptionRevised"));
		transcriptionValidated.setCellValueFactory(new PropertyValueFactory<PageTranscriptionLog, String>("transcriptionValidated"));
		transcriptionTranscriber.setCellValueFactory(new PropertyValueFactory<PageTranscriptionLog, String>("transcriptionTranscriber"));
		
		try{
			PageTranscriptionController.loadTranscriptionLog(TranscriptionWorkProjectQuerySet.loadUUIDDocument(dR.getIdTPrj()));
			if(listLog != null) {
				listLog.clear();
				transcriptionLog.refresh();
			}
			
			for(Page p : PageTranscriptionController.getTranscriptionLog()) {
				listLog.add(
						new PageTranscriptionLog(String.valueOf(p.getPageNumber()), String.valueOf(p.getTranscription().getStaff().getReviser().getValue()), String.valueOf(p.getTranscription().getRevised()), String.valueOf(p.getTranscription().getValidated()), String.valueOf(p.getTranscription().getStaff().getLastTranscriber().getValue())));
			
				System.out.println("CIAO");
			}
			transcriptionLog.setItems(listLog);
			
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	
	public void loadTranscriptionProjectStaff(DocumentRow dR) {
		if (transcriptionProjectStaff != null) {
			transcriptionProjectStaff.clear();
			transcriptionStaff.refresh();
		}

		if (dR.getIdTPrj() != null) {
			TranscriptionProjectController.loadTranscriptionProject(dR.getIdTPrj());

			usernameTranscriber.setCellValueFactory(new PropertyValueFactory<StaffRow, String>("username"));
			roleTranscriber.setCellValueFactory(new PropertyValueFactory<StaffRow, String>("role"));

			transcriptionProjectStaff = FXCollections.observableArrayList();

			for (UUIDUser user : TranscriptionProjectController.getTPrj().getTranscribers()) {
				User u = TranscriptionProjectController.getUserProfile(user);
				transcriptionProjectStaff.add(new StaffRow(u.getUsername(), "Transcriber"));
			}

			for (UUIDUser user : TranscriptionProjectController.getTPrj().getRevisers()) {
				User u = TranscriptionProjectController.getUserProfile(user);
				transcriptionProjectStaff.add(new StaffRow(u.getUsername(), "Reviser"));
			}

			User coordinator = TranscriptionProjectController
					.getUserProfile((TranscriptionProjectController.getTPrj().getCoordinator()));

			System.out.println(coordinator.getUsername());

			transcriptionProjectStaff.add(new StaffRow(coordinator.getUsername(), "Coordinator"));

			transcriptionStaff.setItems(transcriptionProjectStaff);
		} else {

		}
	}

	/*
	 * @FXML private GridPane grid;
	 * 
	 * @FXML public void initialize(){
	 * 
	 * HomePageController.loadMyTranscriptionProjects();
	 * HomePageController.loadMyScanningProjects();
	 * 
	 * int i=1,j=0; for(Entry<UUIDScanningWorkProject, String> entry :
	 * HomePageController.getMySPrj().entrySet()) {
	 * 
	 * if(j==6){ i++; j=0; } Button dd = new Button(entry.getValue());
	 * //dd.setPrefSize(20,50); dd.setMinSize(60, 45); grid.add(dd, j, i, 1, 1);
	 * j++; } Button dd = new Button("+"); dd.setFont(new Font(24));
	 * //dd.setPrefSize(20,50); dd.setMinSize(60, 45); dd.setMaxSize(60, 45);
	 * dd.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
	 * SceneController.loadScene("newDocument"); }); grid.add(dd,0,0,1,1);
	 * 
	 * grid.setHgap(60.0); grid.setVgap(60.0);
	 * 
	 * loadProjects();
	 * 
	 * 
	 * 
	 * 
	 * }
	 * 
	 * public void loadProjects() {
	 * 
	 * /* Image pageIcon=new Image(
	 * "http://www.clker.com/cliparts/3/6/2/6/1348002494474708155New%20Page%20Icon.svg.med.png"
	 * ); ImageView miniature=new ImageView(pageIcon); miniature.setFitWidth(100);
	 * miniature.setFitHeight(140);
	 *
	 * 
	 * int i = 0, j = 0; for (String curr : progetti) { if (j == 6) { i++; j = 0; }
	 * Button dd = new Button(curr); // dd.setPrefSize(20,50); dd.setMinSize(60,
	 * 45); grid.add(dd, j, i, 1, 1); j++; }
	 * 
	 * }
	 */

}
