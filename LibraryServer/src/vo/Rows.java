package vo;

public class Rows {
	private Image img;
	private String number;
	private String revisioned;
	private String validated;
	private UUIDPage id;

	public Rows(String n,String rev,String val) {
		this.number=n;
		this.revisioned=rev;
		this.validated=val;
	}
	
	public Rows(String number,String rev,String val, UUIDPage id,Image img) {
		this.number=number;
		this.revisioned=rev;
		this.validated=val;
		this.setId(id);
		this.img=img;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getRevisioned() {
		return revisioned;
	}

	public void setRevisioned(String revisioned) {
		this.revisioned = revisioned;
	}

	public String getValidated() {
		return validated;
	}

	public void setValidated(String validated) {
		this.validated = validated;
	}

	public UUIDPage getId() {
		return id;
	}

	public void setId(UUIDPage id) {
		this.id = id;
	}

	public Image getImg() {
		return img;
	}

	public void setImg(Image img) {
		this.img = img;
	}
	
	
}
