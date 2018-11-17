package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedList;

import model.Document;
import model.Page;
import model.PageScan;
import model.PageScanStaff;
import model.PageTranscription;
import model.PageTranscriptionStaff;
import vo.DocumentMetadata;
import vo.Image;
import vo.TEItext;
import vo.UUIDDocument;
import vo.UUIDPage;
import vo.UUIDScanningWorkProject;
import vo.UUIDTranscriptionWorkProject;
import vo.UUIDUser;
import vo.VagueDate;

public class DocumentQuerySet {

	/*
	 * Inserisce un nuovo documento
	 * 
	 * @param String Titolo dell'opera
	 * 
	 * @return Boolean: true se l'operazione è andata a buon fine , false altrimenti
	 */
	// TODO questo metodo carica solo il titolo del documento giusto ?
	public static UUIDDocument insertDocument(String title, String author, String description, String composition_date,
			String composition_period_from, String composition_period_to, String preservation_state)
			throws DatabaseException, ParseException {
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}

		if (composition_date != null && (composition_period_from != null || composition_period_to != null)) {
			throw new DatabaseException("Errore inserire la data certa o un periodo");
		} else if (composition_period_from != null && composition_period_to != null && composition_date != null)
			throw new DatabaseException("Errore inserire data certa o un periodo");
		else if (composition_date == null && (composition_period_from == null || composition_period_to == null))
			throw new DatabaseException("Inserire entrambe le dati per il periodo");
		else if (composition_date != null) {
			if (composition_date.length() > 4)
				throw new RuntimeException("Errore inserire anno nel formatto YYYY");

			PreparedStatement ps = null;
			ResultSet rs = null;
			UUIDDocument id = null;

			try {
				con.setAutoCommit(false);
				ps = con.prepareStatement("insert into document(title) value( ? );", new String[] { "ID" });
				ps.setString(1, title);

				ps.addBatch();
				ps.executeBatch();

				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					id = new UUIDDocument(rs.getInt(1));
				}

				con.commit();

				con.setAutoCommit(true);

				ps.close();

				ps = con.prepareStatement("insert into document_metadata(author,description,"
						+ "composition_date,preservation_state,ID_document) values(?,?,?,?,?);");

				ps.setString(1, author);
				ps.setString(2, description);
				ps.setInt(3, Integer.parseInt(composition_date));
				ps.setInt(4, Integer.parseInt(preservation_state));
				ps.setInt(5, id.getValue());

				ps.executeUpdate();

				new File("resources/documents/" + id.getValue()).mkdirs();

			} catch (SQLException e) {
				try {
					con.abort(null);
				} catch (SQLException f) {
					DBConnection.logDatabaseException(new DatabaseException("Duplicato", f));
				}
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (ps != null)
						ps.close();
					if (con != null)
						con.close();
				} catch (SQLException e) {
					DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
				}
			}
			return id;
		} else {
			if (composition_period_from.length() > 4 || composition_period_to.length() > 4)
				throw new RuntimeException("Errore inserire anno nel formatto YYYY");
			if (Integer.parseInt(composition_period_from) > Integer.parseInt(composition_period_to))
				throw new RuntimeException("La data di fine periodo non può precedere quella di inizio");
			PreparedStatement ps = null;
			ResultSet rs = null;
			UUIDDocument id = null;

			try {
				con.setAutoCommit(false);

				ps = con.prepareStatement("insert into document(title) value( ? );", new String[] { "ID" });
				ps.setString(1, title);

				ps.addBatch();
				ps.executeBatch();

				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					id = new UUIDDocument(rs.getInt(1));
				}

				con.commit();

				con.setAutoCommit(true);

				ps.close();

				ps = con.prepareStatement("insert into document_metadata(author,description,"
						+ "composition_period_from,composition_period_to,preservation_state,ID_document) "
						+ "values(?,?,?,?,?,?);");

				ps.setString(1, author);
				ps.setString(2, description);
				ps.setInt(3, Integer.parseInt(composition_period_from));
				ps.setInt(4, Integer.parseInt(composition_period_to));
				ps.setInt(5, Integer.parseInt(preservation_state));
				ps.setInt(6, id.getValue());

				ps.executeUpdate();

				new File("resources/documents/" + id.getValue()).mkdirs();

			} catch (SQLException e) {
				try {
					con.abort(null);
				} catch (SQLException f) {
					DBConnection.logDatabaseException(new DatabaseException("Duplicato", f));
				}
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (ps != null)
						ps.close();
					if (con != null)
						con.close();
				} catch (SQLException e) {
					DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
				}
			}
			return id;
		}
	}

	public static Document loadDocument(UUIDDocument id) throws DatabaseException {
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}

		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		DocumentMetadata dm = null;
		Document d = new Document();

		try {

			// recupero i metadati del documento

			ps = con.prepareStatement("SELECT d.ID AS ID_doc,d.title as doc_title ,dm.* "
					+ "FROM document AS d JOIN document_metadata AS dm ON d.ID=dm.ID_document WHERE d.ID=?;");

			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			rs.last();

			if (rs.getRow() == 1) {
				d.setUUID(id);
				d.setTitle(rs.getString("doc_title"));
				dm = new DocumentMetadata(rs.getString("author"), rs.getString("description"),
						rs.getInt("composition_date"),
						new VagueDate(rs.getInt("composition_period_from"), rs.getInt("composition_period_to")),
						rs.getInt("preservation_state"));

				d.setMetaData(dm);

				// recupero le pagine del documento
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();

				ps = con.prepareStatement(
						"SELECT p.* FROM page AS p JOIN document AS d ON p.ID_document=d.ID WHERE d.ID=?;");
				ps.setInt(1, id.getValue());

				rs = ps.executeQuery();

				LinkedList<Page> page_list = new LinkedList<Page>();

				while (rs.next()) {
					Page p = new Page(new UUIDPage(rs.getInt("ID")), rs.getInt("number"),
							new PageScan(new Image(rs.getString("image")), rs.getBoolean("image_convalidation"),
									rs.getBoolean("image_revised"),
									new PageScanStaff(new UUIDUser(rs.getInt("ID_digitalizer")),
											new UUIDUser(rs.getInt("ID_scanning_reviser")))),
							new PageTranscription(new TEItext(rs.getString("transcription")),
									rs.getBoolean("transcription_revised"),
									rs.getBoolean("transcription_convalidation"),
									rs.getBoolean("transcription_completed"), 
									new PageTranscriptionStaff(
											new UUIDUser(rs.getInt("ID_transcriber")), 
											new UUIDUser(rs.getInt("ID_transcription_reviser"))
											)
									)
							);

					ps1 = con.prepareStatement(
							"SELECT ta.ID_transcriber_user FROM transcription_assigned AS ta JOIN document AS d "
									+ "JOIN page AS p ON d.ID=p.ID_document AND p.ID=ta.ID_page "
									+ "WHERE d.ID=? AND p.ID=?;");
					ps1.setInt(1, id.getValue());
					ps1.setInt(2, rs.getInt("ID"));

					rs1 = ps1.executeQuery();

					LinkedList<UUIDUser> transcriber_user = new LinkedList<UUIDUser>();

					while (rs1.next()) {
						transcriber_user.add(new UUIDUser(rs1.getInt("ID_transcriber_user")));
					}
					p.setPageTranscription(transcriber_user);
					page_list.add(p);

					if (rs1 != null)
						rs1.close();
					if (ps1 != null)
						ps1.close();

				}
				d.setPageList(page_list);

				// recupero l'ID dello scanning project e del transcription project
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();

				ps = con.prepareStatement("SELECT sp.ID AS scan_prj_ID, tp.ID AS trans_prj_ID "
						+ "FROM document AS d JOIN scanning_project AS sp JOIN transcription_project AS tp "
						+ "ON d.ID=sp.ID_document and d.ID=tp.ID_document WHERE d.ID=?;");

				ps.setInt(1, id.getValue());
				rs = ps.executeQuery();

				rs.last();

				if (rs.getRow() == 1) {
					d.setScanningProject(new UUIDScanningWorkProject(rs.getInt("scan_prj_ID")));
					d.setTranscriptionWorkProject(new UUIDTranscriptionWorkProject(rs.getInt("trans_prj_ID")));
				} else if (rs.getRow() == 0) {
					d.setScanningProject(null);
					d.setTranscriptionWorkProject(null);
				} else {
					throw new DatabaseException("Errore: è stata trova più di una corrispondenza per i project");
				}

			} else if (rs.getRow() == 0) {
				throw new DatabaseException("Errore non è stato trovato un documento corrispondente all'id" + id);
			} else {

				throw new DatabaseException(
						"Errore: è stata rilevata più di una corrispondenza per l'id" + id.getValue());
			}

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione della query: " + e.getMessage(), e);
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (ps1 != null)
					ps1.close();
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}

		}

		return d;
	}

	// aggiorna un documento
	public void updateDocument() {

	}

	// cancella un documento
	public void deleteDocument() {

	}

	// scarica un documento
	public void downloadFileDocument() {

	}
	/*
	public static void main(String[] args) {
		try {
			DocumentQuerySet.loadDocument(new UUIDDocument(142));

		} catch (Exception e) {
			System.out.println(e);
		}
	}*/

}
