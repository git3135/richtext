package com.onpositive.richtext.model.meta;

public final class Font {

	public final int height;
	public final String family;
	public final int style;
	
	public Font(String string, int height, int style) {
		this.height=height;
		this.family=string;
		this.style=style;
	}

	public float getHeight(){
		return height;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + height;
		result = prime * result + style;
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Font other = (Font) obj;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
			return false;
		if (height != other.height)
			return false;
		if (style != other.style)
			return false;
		return true;
	}

	public String getName() {
		return family;
	}
	
	public String toString(){
		return "FontStyle{"+family+"-"+height+"-"+style+"}";
	}

	public int getStyle() {
		return style;
	}
}
