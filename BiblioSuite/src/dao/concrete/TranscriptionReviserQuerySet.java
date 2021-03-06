package dao.concrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dao.interfaces.TranscriptionReviserQuerySetDAO;
import vo.UUIDPage;
import vo.UUIDTranscriptionWorkProject;

public class TranscriptionReviserQuerySet implements TranscriptionReviserQuerySetDAO{
	
	/* Segnala completato un TranscriptionWorkProject
	 * @param UUIDTranscriptionWorkProject Id del TranscriptionWorkProject da segnalare completato
	 * @return Boolean true se la modifica è andata a buon fine
	 * @exception DatabaseException in caso di errori nella connessione con il DB o di esecuzione query
	 */
	public  Boolean completed(UUIDTranscriptionWorkProject id) throws DatabaseException {
		Connection con = null;
		
		try {
			con = DBConnection.connect();
		}catch(DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}
		
		PreparedStatement ps = null;
		Integer result = null;
		
		try {
			ps = con.prepareStatement("UPDATE transcription_project SET transcription_complete = 1 WHERE id=?");
			ps.setInt(1, id.getValue());
			
			result = ps.executeUpdate();
			
		}catch(SQLException e) {
			throw new DatabaseException("Errore di esecuzione della query", e);
		}finally {
			try{
				if(ps != null)
					ps.close();
				if(con!=null)
					con.close();
			}catch(SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
			
			
		}
		
		return (result == 1);
		
	}
	
	/*Aggiorna la transcrizione di una Page come completata
	 * @param UUIDPage Id della pagina di cui si deve convalidare la trascrizione
	 * @return Boolean true se l'operazione è andata a buon fine
	 * @exception DatabaseException in caso di errori nella connessione con il DB o di esecuzione query 
	 */
	
	public  Boolean validated(UUIDPage id, boolean b) throws DatabaseException {
		Connection con = null;
		
		try {
			con = DBConnection.connect();
		}catch(DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}
		
		PreparedStatement ps = null;
		Integer result = null;
		
		try {
			ps = con.prepareStatement("UPDATE page SET transcription_convalidation = ? WHERE id=?");
			ps.setBoolean(1, b);
			ps.setInt(2, id.getValue());
			
			result = ps.executeUpdate();
			
		}catch(SQLException e) {
			throw new DatabaseException("Errore di esecuzione della query", e);
		}finally {
			try{
				if(ps != null)
					ps.close();
				if(con!=null)
					con.close();
			}catch(SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
			
			
		}
		
		return (result == 1);
		
	}
	
	/*aggiorna una trascrizione come revisionata
	 *@param UUIDPage Id della pagina di cui si deve convalidare la trascrizione
	 * @return Boolean true se l'operazione è andata a buon fine
	 * @exception DatabaseException in caso di errori nella connessione con il DB o di esecuzione query 
	 */
	public  Boolean revised(UUIDPage id, boolean b) throws DatabaseException{
		Connection con = null;
		
		try {
			con = DBConnection.connect();
		}catch(DatabaseException e) {
			throw new DatabaseException("Errore di connessione", e);
		}
		
		PreparedStatement ps = null;
		Integer result = null;
		
		try {
			ps = con.prepareStatement("UPDATE page SET transcription_revised = ? WHERE id=?");
			ps.setBoolean(1, b);
			ps.setInt(2, id.getValue());
			
			result = ps.executeUpdate();
			
		}catch(SQLException e) {
			throw new DatabaseException("Errore di esecuzione della query", e);
		}finally {
			try{
				if(ps != null)
					ps.close();
				if(con!=null)
					con.close();
			}catch(SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
			
			
		}
		
		return (result == 1);
	}
	
	/**
	 * Permette di aggiungere un commento all'ultima trascrizione della pagina in sede di revisione
	 * @param id
	 * @param comment
	 * @throws DatabaseException
	 */
	public  void addTranscriptionRevisionComment(UUIDPage id, String comment) throws DatabaseException {

		Connection con = null;
		
		try {
			con = DBConnection.connect();
		}catch(DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}
		
		PreparedStatement ps = null;
	
		try {
			ps = con.prepareStatement("UPDATE page SET transcription_reviser_comment=? WHERE ID=?");
			ps.setString(1, comment);
			ps.setInt(2, id.getValue());
			
			ps.executeUpdate();
			
		}catch(SQLException e) {
			throw new DatabaseException("Errore di esecuzione query", e);
		}finally {
			try {
				if(ps != null)
					ps.close();
				if(con != null)
					con.close();
			}catch(SQLException e) {
				DBConnection.logDatabaseException(new DatabaseException("Errore sulle risorse", e));
			}
		}
	}
	
	/**
	 * Permette di accedere al commento dell'ultima trascrizione della pagina
	 * 
	 * @param id
	 * @param comment
	 * @throws DatabaseException
	 */
	public  String getTranscriptionRevisionComment(UUIDPage id) throws DatabaseException {

		Connection con = null;

		try {
			con = DBConnection.connect();
		} catch (DatabaseException ex) {
			throw new DatabaseException("Errore di connessione", ex);
		}

		PreparedStatement ps = null;
		String comment = "Comment Not Found";
		ResultSet rs = null;
		
		try {
			ps = con.prepareStatement("SELECT transcription_reviser_comment FROM page WHERE ID=?");
			ps.setInt(1, id.getValue());

			rs = ps.executeQuery();
			
			if(rs.next()) {
				comment = rs.getString("transcription_reviser_comment");
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
		return comment;
	}

}
