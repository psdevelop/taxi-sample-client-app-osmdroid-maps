package ru.psdevelop.tdclientapp;

import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;

public class Employee implements Parcelable {

	private int id;
	private String name;
	private Date dateOfBirth;
	private double salary;

	public Employee() {
		super();
	}

	private Employee(Parcel in) {
		super();
		this.id = in.readInt();
		this.name = in.readString();
		this.dateOfBirth = new Date(in.readLong());
		this.salary = in.readDouble();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", name=" + name + ", dateOfBirth="
				+ dateOfBirth + ", salary=" + salary + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Employee other = (Employee) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(getId());
		parcel.writeString(getName());
		parcel.writeLong(getDateOfBirth().getTime());
		parcel.writeDouble(getSalary());
	}

	public static final Parcelable.Creator<Employee> CREATOR = new Parcelable.Creator<Employee>() {
		public Employee createFromParcel(Parcel in) {
			return new Employee(in);
		}

		public Employee[] newArray(int size) {
			return new Employee[size];
		}
	};

}
