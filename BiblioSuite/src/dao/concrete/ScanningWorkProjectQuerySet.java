package dao.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dao.interfaces.ScanningWorkProjectQuerySetDAO;
import model.ScanningWorkProject;
import vo.UUIDDocument;
import vo.UUIDScanningWorkProject;
import vo.UUIDUser;

public class ScanningWorkProjectQuerySet implements ScanningWorkProjectQuerySetDAO{

	// iscerisce un nuovo ScanningWorkProject nel DB con il relativo personale
	public  UUIDScanningWorkProject insertScanningWorkProject(UUIDUser coordinator, Boolean completed,
			LinkedList<UUIDUser> digitalizers, LinkedList<UUIDUser> revisers, UUIDDocument ID_doc)
			throws DatabaseException {

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		UUIDScanningWorkProject id = null;

		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(
					"insert into scanning_project(ID_coordinator, ID_document,scanning_complete) values (?,?,?);",
					new String[] { "ID" });
			ps.setInt(1, coordinator.getValue());
			ps.setInt(2, ID_doc.getValue());
			ps.setBoolean(3, completed);

			ps.addBatch();
			ps.executeBatch();

			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = new UUIDScanningWorkProject(rs.getInt(1));
			}
			con.commit();
			con.setAutoCommit(true);

			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();

			Iterator<UUIDUser> itr = digitalizers.iterator();

			while (itr.hasNext()) {
				UUIDUser usr = itr.next();
				ps = con.prepareStatement("insert into "
						+ "scanning_project_digitalizer_partecipant(ID_scanning_project, ID_digitalizer_user) "
						+ "values (?,?);");
				ps.setInt(1, id.getValue());
				ps.setInt(2, usr.getValue());
				rs = ps.executeQuery();
			}

			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();

			itr = revisers.iterator();

			while (itr.hasNext()) {
				UUIDUser usr = itr.next();
				ps = con.prepareStatement(
						"insert into " + "scanning_project_reviser_partecipant(ID_scanning_project, ID_reviser_user) "
								+ "values (?,?);");
				ps.setInt(1, id.getValue());
				ps.setInt(2, usr.getValue());

			}

		} catch (SQLException e) {
			try {
				con.abort(null);
			} catch (SQLException f) {
				DBConnection.logDatabaseException(new DatabaseException("Duplicato", f));
			}
		} finally {
			try {
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

	/*
	 * Seleziona uno ScanningWorkProject con il relativo personale
	 * 
	 * @param UUIDTranscriptionProject id del progetto da caricare
	 * 
	 * @return TranscriptionWorkProject oggetto completo
	 */

	public  ScanningWorkProject loadScanningWorkProject(UUIDScanningWorkProject id)
			throws DatabaseException, NullPointerException {
		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		ScanningWorkProject swp = null;
		LinkedList<UUIDUser> digitalizers = new LinkedList<UUIDUser>();
		LinkedList<UUIDUser> revisers = new LinkedList<UUIDUser>();

		try {
			ps = con.prepareStatement("SELECT ID_digitalizer_user FROM scanning_project_digitalizer_partecipant "
					+ "WHERE ID_scanning_project = ?;");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			while (rs.next()) {
				digitalizers.add(new UUIDUser(rs.getInt("ID_digitalizer_user")));
			}

			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();

			ps = con.prepareStatement("SELECT ID_reviser_user FROM scanning_project_reviser_partecipant "
					+ "WHERE ID_scanning_project=?;");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			while (rs.next()) {
				revisers.add(new UUIDUser(rs.getInt("ID_reviser_user")));
			}
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();

			ps = con.prepareStatement(
					"SELECT ID_coordinator,date,scanning_complete " + "FROM scanning_project WHERE ID=?;");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			if (rs.next()) {
				if (rs.getDate("date") != null) {

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date = rs.getString("date");
					Date d = sdf.parse(date);

					swp = new ScanningWorkProject(d, new UUIDUser(rs.getInt("ID_coordinator")), digitalizers, revisers,
							id, rs.getBoolean("scanning_complete"));

				} else {
					swp = new ScanningWorkProject(null, new UUIDUser(rs.getInt("ID_coordinator")), digitalizers,
							revisers, id, rs.getBoolean("scanning_complete"));

				}
			}
		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} catch (ParseException pe) {
			throw new DatabaseException("Errore nel parsing della data", pe);
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

		return swp;
	}

	/**
	 * Selects the UUIDDocument of a specific UUIDScanningWorkProject
	 * 
	 * @param UUIDScanningWorkProject id corresponding to the Document
	 * 
	 * @return UUIDDocument UUID of the Document
	 */

	public  UUIDDocument loadUUIDDocument(UUIDScanningWorkProject id)
			throws DatabaseException, NullPointerException {
		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		UUIDDocument doc = null;

		try {
			ps = con.prepareStatement("SELECT ID_document FROM scanning_project " + "WHERE ID = ?;");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			if (rs.next())
				doc = new UUIDDocument(rs.getInt("ID_document"));

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
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
		return doc;
	}

	/*
	 * Aggiungi un utente Digitalizzatore ad un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto in cui si deve inserire
	 * l'utente
	 * 
	 * @param UUIDUser ID dell'utente da inserire
	 * 
	 * @return Boolean true se l'utente è stato inserito correttamente nel progetto
	 * di digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Boolean insertDigitalizerUser(UUIDScanningWorkProject ids, UUIDUser id)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");

		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement("INSERT INTO scanning_project_digitalizer_partecipant "
					+ "(ID_scanning_project,ID_digitalizer_user) VALUE (?,?);");
			ps.setInt(1, ids.getValue());
			ps.setInt(2, id.getValue());

			return (ps.executeUpdate() == 1);

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/*
	 * Aggiungi un utente Revisore ad un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto in cui si deve inserire
	 * l'utente
	 * 
	 * @param UUIDUser ID dell'utente da inserire
	 * 
	 * @return Boolean true se l'utente è stato inserito correttamente nel progetto
	 * di digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Boolean insertReviserUser(UUIDUser id, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");

		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement("INSERT INTO scanning_project_reviser_partecipant "
					+ "(ID_scanning_project,ID_reviser_user) VALUE (?,?);");
			ps.setInt(1, ids.getValue());
			ps.setInt(2, id.getValue());

			return (ps.executeUpdate() == 1);

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}

	}

	/*
	 * Rimuove un utente Digitalizzatore da un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto in cui si deve inserire
	 * l'utente
	 * 
	 * @param UUIDUser ID dell'utente da inserire
	 * 
	 * @return Boolean true se l'utente è stato eliminato correttamente nel progetto
	 * di digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Boolean removeDigitalizerUser(UUIDUser id, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");

		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement("DELETE FROM scanning_project_digitalizer_partecipant "
					+ "WHERE ID_scanning_project = ? AND ID_digitalizer_user =?;");
			ps.setInt(1, ids.getValue());
			ps.setInt(2, id.getValue());

			return (ps.executeUpdate() == 1);

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/*
	 * Rimuove un utente Revisore da un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto in cui si deve inserire
	 * l'utente
	 * 
	 * @param UUIDUser ID dell'utente da inserire
	 * 
	 * @return Boolean true se l'utente è stato eliminato correttamente nel progetto
	 * di digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Boolean removeReviserUser(UUIDUser id, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");

		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement("DELETE FROM scanning_project_reviser_partecipant "
					+ "WHERE ID_scanning_project = ? AND ID_reviser_user =?;");
			ps.setInt(1, ids.getValue());
			ps.setInt(2, id.getValue());

			return (ps.executeUpdate() == 1);

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}

	}

	/*
	 * Inserisce una lista di utenti Digitalizzatori in un progetto di
	 * Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto in cui si devono inserire gli
	 * utenti
	 * 
	 * @param LinkedList<UUIDUser> ID degli utenti da inserire
	 * 
	 * @return Integer numero di Digitalizzatori correttamente inseriti nel progetto
	 * di digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Integer insertDigitalizerUser(LinkedList<UUIDUser> idl, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (idl == null)
			throw new NullPointerException("Lista di utenti non valida");
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		Iterator<UUIDUser> itr = idl.iterator();

		Integer linesAffected = 0;

		try {
			ps = con.prepareStatement(
					"INSERT INTO scanning_project_digitalizer_partecipant(ID_scanning_project,ID_digitalizer_user) "
							+ "VALUE (?,?);");
			ps.setInt(1, ids.getValue());

			while (itr.hasNext()) {
				Integer user = itr.next().getValue();
				ps.setInt(2, user);

				linesAffected += ps.executeUpdate();
			}

			return linesAffected;

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/*
	 * Inserisce una lista di utenti Revisori in un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto in cui si devono inserire gli
	 * utenti
	 * 
	 * @param LinkedList<UUIDUser> ID degli utenti da inserire
	 * 
	 * @return Integer numero di Revisori correttamente inseriti nel progetto di
	 * digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Integer insertReviserUser(LinkedList<UUIDUser> idl, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (idl == null)
			throw new NullPointerException("Lista di utenti non valida");
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		Iterator<UUIDUser> itr = idl.iterator();

		Integer linesAffected = 0;

		try {
			ps = con.prepareStatement(
					"INSERT INTO scanning_project_reviser_partecipant(ID_scanning_project,ID_reviser_user) "
							+ "VALUE (?,?);");
			ps.setInt(1, ids.getValue());

			while (itr.hasNext()) {
				Integer user = itr.next().getValue();
				ps.setInt(2, user);

				linesAffected += ps.executeUpdate();
			}

			return linesAffected;

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/*
	 * Rimuoveuna lista di utenti Digitalizzatori da un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto da cui si devono eliminare gli
	 * utenti
	 * 
	 * @param LinkedList<UUIDUser> ID degli utenti da eliminare
	 * 
	 * @return Integer numero di Digitalizzatori correttamente rimossi dal progetto
	 * di digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Integer removeDigitalizerUser(LinkedList<UUIDUser> idl, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (idl == null)
			throw new NullPointerException("Lista di utenti non valida");
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		Iterator<UUIDUser> itr = idl.iterator();

		Integer linesAffected = 0;

		try {
			ps = con.prepareStatement("DELETE FROM scanning_project_digitalizer_partecipant "
					+ "WHERE ID_scanning_project=? AND ID_digitalizer_user=?");
			ps.setInt(1, ids.getValue());

			while (itr.hasNext()) {
				Integer user = itr.next().getValue();
				ps.setInt(2, user);

				linesAffected += ps.executeUpdate();
			}

			return linesAffected;

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/*
	 * Rimuoveuna lista di utenti Revisori da un progetto di Digitalizzazione
	 * 
	 * @param UUIDScanningWorkProject ID del progetto da cui si devono eliminare gli
	 * utenti
	 * 
	 * @param LinkedList<UUIDUser> ID degli utenti da eliminare
	 * 
	 * @return Integer numero di Revisori correttamente rimossi dal progetto di
	 * digitalizzazione
	 * 
	 * @throw DatabaseException in caso di errori di connessione ad DB o sulle query
	 * 
	 * @throws NullPointerException in caso i parametri di input siano nulli
	 */
	public  Integer removeReviserUser(LinkedList<UUIDUser> idl, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (idl == null)
			throw new NullPointerException("Lista di utenti non valida");
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		Iterator<UUIDUser> itr = idl.iterator();

		Integer linesAffected = 0;

		try {
			ps = con.prepareStatement("DELETE FROM scanning_project_reviser_partecipant "
					+ "WHERE ID_scanning_project=? AND ID_reviser_user=?");
			ps.setInt(1, ids.getValue());

			while (itr.hasNext()) {
				Integer user = itr.next().getValue();
				ps.setInt(2, user);

				linesAffected += ps.executeUpdate();
			}

			return linesAffected;

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/*
	 * Cancella un TranscriptionWorkProject con il relativo personale
	 * 
	 * @param UUIDScanningWorkProject ID del progetto da eliminare
	 * 
	 * @return Boolean true se il Progetto di Digitalizzazione è stato correttamente
	 * eliminato
	 * 
	 * @throw DatabaseException in caso di errori di connessione al DB o di
	 * esecuzione di query
	 * 
	 * @throw NullPointerException In caso il parametro di input sia null
	 */
	public  Boolean deleteScanningWorkProject(UUIDScanningWorkProject id)
			throws DatabaseException, NullPointerException {
		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement("DELETE FROM scanning_project WHERE ID=?;");
			ps.setInt(1, id.getValue());

			return (ps.executeUpdate() == 1);

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}

	/**
	 * Permette di estrarre tutti i documenti il cui scanning project non è
	 * completato e l'utente parametro ne è un digitalizzatore
	 * 
	 * @param id
	 * @return
	 * @throws DatabaseException
	 */
	public  HashMap<UUIDDocument, String> getScanningUncompletedDocumentDigitalizer(UUIDUser id) throws DatabaseException {

		HashMap<UUIDDocument, String> uncompletedDocument = new HashMap<UUIDDocument, String>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		try {
			ps = con.prepareStatement("select d.ID as ID ,d.title as T from document as d "
					+ "join scanning_project as sp join scanning_project_digitalizer_partecipant  "
					+ "as spp on d.ID=sp.ID_document  AND spp.ID_scanning_project=sp.ID "
					+ "WHERE scanning_complete=0 AND spp.ID_digitalizer_user = ?;");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			while (rs.next()) {
				uncompletedDocument.put(new UUIDDocument(rs.getInt("ID")), rs.getString("T"));
			}

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
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

		return uncompletedDocument;
	}
	
	/**
	 * Permette di estrarre tutti i documenti il cui scanning project non è
	 * completato e l'utente parametro ne è un revisore
	 * 
	 * @param id
	 * @return
	 * @throws DatabaseException
	 */
	public  HashMap<UUIDDocument, String> getScanningUncompletedDocumentReviser(UUIDUser id) throws DatabaseException {

		HashMap<UUIDDocument, String> uncompletedDocument = new HashMap<UUIDDocument, String>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		try {
			ps = con.prepareStatement("select d.ID as ID ,d.title as T from document as d "
					+ "join scanning_project as sp join scanning_project_reviser_partecipant  "
					+ "as spp on d.ID=sp.ID_document  AND spp.ID_scanning_project=sp.ID "
					+ "WHERE scanning_complete=0 AND spp.ID_reviser_user = ?;");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();

			while (rs.next()) {
				uncompletedDocument.put(new UUIDDocument(rs.getInt("ID")), rs.getString("T"));
			}

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
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

		return uncompletedDocument;
	}
	/*
	 * public  void main(String[] args) {
	 * 
	 * try{
	 * System.out.println(ScanningWorkProjectQuerySet.getScanningUncompletedDocument
	 * (new UUIDUser(61))); }catch(DatabaseException e) { e.getMessage(); } }
	 */

	/**
	 * seleziona i progetti di digitalizzazione ai quali lavoro (ovvero progetti che
	 * non sono stati ancora pubblicati)
	 * 
	 * @param UUIDUser utente di cui si devono reperire i progetti di
	 *                 digitalizzazione
	 * @return HashMap<Integer,String> Mappa di Id progetti di digitalizzazione e
	 *         titolo dell'opera associata
	 */
	public  HashMap<UUIDScanningWorkProject, String[]> loadMyCoordinatedScanningWorkProjectList(UUIDUser usr)
			throws DatabaseException {
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		HashMap<UUIDScanningWorkProject, String[]> swpl = new HashMap<UUIDScanningWorkProject, String[]>();

		try {
			ps = con.prepareStatement("SELECT sp.ID as ID_progetto, d.title as Title , sp.scanning_complete "
					+ "FROM scanning_project as sp "
					+ "JOIN document as d ON sp.ID_document=d.ID WHERE ID_coordinator=?;");
			ps.setInt(1, usr.getValue());

			rs = ps.executeQuery();

			while (rs.next()) {
				String[] array = { rs.getString("Title"), String.valueOf(rs.getBoolean("sp.scanning_complete")) };
				swpl.put(new UUIDScanningWorkProject(rs.getInt("ID_progetto")), array);
			}

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione della query", e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}

		}

		return swpl;

	}

	public  boolean ifExistScanningProject(UUIDDocument doc) throws DatabaseException {
		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;

		Boolean exist = false;

		try {
			ps = con.prepareStatement(
					"SELECT IF( EXISTS(SELECT * FROM scanning_project WHERE ID_document = ?), 1, 0) as Exist;");
			ps.setInt(1, doc.getValue());

			rs = ps.executeQuery();

			if (rs.next()) {
				exist = rs.getBoolean("Exist");
			}

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione della query", e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}

		}

		return exist;

	}

	/**
	 * cambia il coordinatore di un progetto di scansione
	 * 
	 * @param id
	 * @param ids
	 * @return
	 * @throws DatabaseException
	 * @throws NullPointerException
	 */
	public  Boolean changeScanningProjectCoordinator(UUIDUser id, UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");

		if (id == null)
			throw new NullPointerException("Id non vallido");

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement("update scanning_project set ID_coordinator = ? where ID = ?;");
			ps.setInt(1, id.getValue());
			ps.setInt(2, ids.getValue());

			return (ps.executeUpdate() == 1);

		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}
	
	/**
	 * Recupera tutti gli utenti attivi, che soni digitalizzatori e che non fanno già parte del progetto sia come digitalizzatori che come revisori che come coordinatori 
	 * @param ids
	 * @return
	 * @throws DatabaseException
	 * @throws NullPointerException
	 */
	public  LinkedList<UUIDUser> getAvailableDigitalizers(UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");


		Connection con = null;
		LinkedList<UUIDUser> availableStaff = new LinkedList<UUIDUser>();
		
		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;
		
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("select u.ID from user as u join perm_authorization as perm "
					+ "on u.ID=perm.ID_user "
					+ "WHERE upload=1 and status = 1 "
					+ "and u.ID not in( select ID_digitalizer_user from scanning_project_digitalizer_partecipant where ID_scanning_project=?) "
					+ "AND u.ID not in( select ID_reviser_user from scanning_project_reviser_partecipant where ID_scanning_project=?)"
					+ "AND u.ID not in( select ID_coordinator from scanning_project where ID=?) ;");
			ps.setInt(1, ids.getValue());
			ps.setInt(2, ids.getValue());
			ps.setInt(3, ids.getValue());

			rs = ps.executeQuery();
			
			while(rs.next()) {
				availableStaff.add(new UUIDUser(rs.getInt("ID")));
			}
			
			
		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if(rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
		return availableStaff;
	}
	
	/**
	 * Recupera tutti gli utenti attivi, che sono revisori e che non fanno già parte del progetto sia come digitalizzatori che come revisori e coordinatori
	 * @param ids
	 * @return
	 * @throws DatabaseException
	 * @throws NullPointerException
	 */
	public  LinkedList<UUIDUser> getAvailableRevisers(UUIDScanningWorkProject ids)
			throws DatabaseException, NullPointerException {
		if (ids == null)
			throw new NullPointerException("Id Progetto non valido");


		Connection con = null;
		LinkedList<UUIDUser> availableStaff = new LinkedList<UUIDUser>();
		
		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;
		
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("select u.ID from user as u join perm_authorization as perm "
					+ "on u.ID=perm.ID_user "
					+ "WHERE reviewPage=1 and status = 1 and "
					+ "u.ID not in( select ID_digitalizer_user from scanning_project_digitalizer_partecipant where ID_scanning_project=?) "
					+ "AND u.ID not in( select ID_reviser_user from scanning_project_reviser_partecipant where ID_scanning_project=?) "
					+ "AND u.ID not in( select ID_coordinator from scanning_project where ID=?) ;");
			ps.setInt(1, ids.getValue());
			ps.setInt(2, ids.getValue());
			ps.setInt(3, ids.getValue());

			rs = ps.executeQuery();
			
			while(rs.next()) {
				availableStaff.add(new UUIDUser(rs.getInt("ID")));
			}
			
			
		} catch (SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		} finally {

			try {
				if(rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
		return availableStaff;
	}
	

}
