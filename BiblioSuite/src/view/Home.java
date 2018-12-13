package view;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map.Entry;

import controller.HomePageController;
import controller.PageViewController;
import dao.DatabaseException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.LoadException;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import vo.UUIDDocument;
import vo.UUIDDocumentCollection;
import vo.UUIDScanningWorkProject;
import vo.view.DocumentRow;

public class Home {

	@FXML
	private AnchorPane topbar;

	@FXML
	private HBox sProjects;

	@FXML
	private HBox tProjects;

	@FXML
	private HBox bookmarks;

	@FXML
	private VBox news;

	@FXML
	private VBox collections;

	@FXML
	private Tab transcriptionTabHome;

	@FXML
	private Tab scanningTabHome;

	@FXML
	private Accordion documentCollections;
	
	@FXML
	private TextField searchBar;

	@FXML
	public void initialize() throws DatabaseException, ParseException {
		loadNews();
		loadCollections();
		loadScanningProjects();
		loadTranscriptionProjects();
		loadBookmarks();
		initSearchBar();
	}
	
	@FXML
	public void initSearchBar() {
		searchBar.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				int l=newValue.length();
				/*if(l>1) {
					if (newValue.substring(l-1,l).equals(" ") && newValue.replaceAll("\\s","").length()>0)
						System.out.println("doca");
				}*/
				
				/*if(l>1) {
					if (newValue.substring(l-1,l).equals(" ") && !(newValue.replaceAll("\\s","").length()>0))
					{
						searchBar.setText("");
						System.out.println("tuc");
					}
					
				}*/
			}
		});
	}

	@FXML
	public void loadScanningProjects() {

		if (HomePageController.checkIsDigitalizer()) {

			HomePageController.loadMyScanningProjects();

			Image pageIcon = new Image("images/blank.png");
			for (Entry<UUIDScanningWorkProject, String[]> entry : HomePageController.getMySPrj().entrySet()) {
				ImageView miniature = new ImageView(pageIcon);
				Label label = new Label(entry.getValue()[0]);
				VBox elem = new VBox();

				miniature.setFitWidth(100);
				miniature.setFitHeight(140);
				elem.setId("project-miniature");
				elem.getChildren().add(miniature);
				elem.getChildren().add(label);
				elem.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
					try {
						LoadScan.setToOpenDocumentFromScanningProject(entry.getKey());
						SceneController.loadScene("loadScan");
					} catch (NullPointerException | DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				sProjects.getChildren().add(elem);
			}
		} else {
			scanningTabHome.setDisable(true);
		}
	}

	@FXML
	public void loadTranscriptionProjects() {
		if (HomePageController.checkIsTranscriber()) {

			HomePageController.loadMyTranscriptionProjects();

			Image pageIcon = new Image("images/blank.png");
			for (String[] text : HomePageController.getMyTPrj().values()) {
				ImageView miniature = new ImageView(pageIcon);
				Label label = new Label(text[0]);
				VBox elem = new VBox();

				miniature.setFitWidth(100);
				miniature.setFitHeight(140);
				elem.setId("project-miniature");
				elem.getChildren().add(miniature);
				elem.getChildren().add(label);
				tProjects.getChildren().add(elem);
			}
		} else {
			transcriptionTabHome.setDisable(true);
		}
	}

	@FXML
	public void loadBookmarks() {
		HomePageController.loadMyCollection();

		// TODO prendere la vera immagine dal String[] appena disponibili
		Image pageIcon = new Image("images/libricino.png");

		for (Entry<UUIDDocument, String[]> entry : HomePageController.getMyCollection().entrySet()) {
			ImageView miniature = new ImageView(pageIcon); // cambiare con entry[0]
			Label label = new Label(entry.getValue()[0]);
			VBox elem = new VBox();

			miniature.setFitWidth(100);
			miniature.setFitHeight(140);
			elem.setId("project-miniature");
			elem.getChildren().add(miniature);
			elem.getChildren().add(label);

			elem.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

				try {
					PageViewController.showDocument(entry.getKey());
				} catch (LoadException e) {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Error");
					alert.setContentText(e.getMessage());
					alert.show();
					e.printStackTrace();
				}

				event.consume();
			});

			bookmarks.getChildren().add(elem);
		}

	}

	
	@FXML
	public void loadNews() throws DatabaseException, ParseException {
		try {
			HomePageController.loadNews();
			HashMap<UUIDDocument, String[]> newsMap = HomePageController.getNews();
			int c = 0;

			for (Entry<UUIDDocument, String[]> entry : newsMap.entrySet()) {

				Label title1 = new Label(entry.getValue()[0]);
				title1.setPrefWidth(150);
				Label title2 =  new Label("( " + entry.getValue()[1] + " giorni fa )");
				title2.setPrefWidth(150);
				Label title3 = new Label(entry.getValue()[2]);
				
				String s=title1.getText();
				char first = Character.toUpperCase(s.charAt(0));
				String s1 = first + s.substring(1);
				title1.setText(s1);
			
		
				HBox row = new HBox();

				if (c % 2 == 0)
					row.setId("news-row");
				else
					row.setId("news-row1");
				row.getChildren().add(title1);
				row.getChildren().add(title2);
				row.getChildren().add(title3);


				row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
					try {
						PageViewController.showDocument(entry.getKey());
					} catch (LoadException e) {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setContentText(e.getMessage());
						alert.show();
						e.printStackTrace();
					}
					event.consume();
				});

				news.getChildren().add(row);
				c++;
			}
		} catch (DatabaseException e) {
			Label label = new Label(e.getMessage());
			news.getChildren().add(label);

		}
	}

	@FXML
	public void loadCollections() {
		// carico le collezioni
		HomePageController.loadCategories();

		for (Entry<UUIDDocumentCollection, String> title : HomePageController.getCategories().entrySet()) {
			// creo il nome della collezione
			TitledPane tp = new TitledPane();
			// imposto il titolo della collezione
			tp.setText(title.getValue());
			// carico i documenti della collezione
			HomePageController.loadDocumentInCollections(title.getKey());
			// creo il contenitore per i documenti
			ListView<DocumentRow> lista = new ListView<DocumentRow>();
			ObservableList<DocumentRow> oss = FXCollections.observableArrayList();
			// imposto la visualizzazione della cella
			lista.setCellFactory(lv -> new ListCell<DocumentRow>() {
				@Override
				protected void updateItem(DocumentRow item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty || item == null ? "" : item.getDocument());
				}
			});

			lista.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DocumentRow>() {

				@Override
				public void changed(ObservableValue<? extends DocumentRow> observable, DocumentRow oldValue,
						DocumentRow newValue) {
					
					try {
						PageViewController.showDocument(newValue.getId());
					} catch (LoadException e) {
						e.printStackTrace();
						System.out.println(e.getMessage());
					}

				}

			});
			/*
			 * leaderListView.getSelectionModel().selectedItemProperty() .addListener(new
			 * ChangeListener<Person>() { public void changed(ObservableValue<? extends
			 * Person> observable, Person oldValue, Person newValue) {
			 * System.out.println("selection changed"); } });
			 */

			for (String[] document : HomePageController.getListDocumentInCollections()) {

				oss.add(new DocumentRow(document[1], new UUIDDocument(Integer.parseInt(document[0]))));

			}
			// aggiungo la lista di documenti al listview
			lista.setItems(oss);
			// inserisco la listview nel pane
			tp.setContent(lista);
			// aggiungo il pane nell'accordion
			documentCollections.getPanes().add(tp);
		}
	}

}
