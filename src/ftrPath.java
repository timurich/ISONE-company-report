public class ftrPath 
{
	private String source;
	private String sink;
	private String type;	
	private double ftrPrice;
	
 	public ftrPath(String source, String sink, String type, double price) {	
		this.source = source;
		this.sink = sink;
		this.type = type;	
		ftrPrice = price;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSink() {
		return sink;
	}
	public void setSink(String sink) {
		this.sink = sink;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setFtrPrice(double price) {
		ftrPrice = price;
	}
	public double getFtrPrice(){
		return ftrPrice;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sink == null) ? 0 : sink.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ftrPath other = (ftrPath) obj;
		if (sink == null) {
			if (other.sink != null)
				return false;
		} else if (!sink.equals(other.sink))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}	
}
