package ru.psdevelop.tdclientapp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class EmpListFragment extends Fragment implements OnItemClickListener
		//OnItemLongClickListener
{

	public static final String ARG_ITEM_ID = "employee_list";

	Activity activity;
	ListView employeeListView;
	ArrayList<Employee> employees;

	EmpListAdapter employeeListAdapter;
	EmployeeDAO employeeDAO;

	private GetEmpTask task;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
		employeeDAO = new EmployeeDAO(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hist_layout, container,
				false);
		findViewsById(view);

		task = new GetEmpTask(activity);
		task.execute((Void) null);
		ArrayList<Employee> empList = employeeDAO.getEmployees();
		employeeListAdapter = new EmpListAdapter(activity,
				empList);
		employeeListView.setAdapter(employeeListAdapter);

		employeeListView.setOnItemClickListener(this);
		//employeeListView.setOnItemLongClickListener(this);
		// Employee e = employeeDAO.getEmployee(1);
		// Log.d("employee e", e.toString());
		return view;
	}

	private void findViewsById(View view) {
		employeeListView = (ListView) view.findViewById(R.id.historyListView);
	}

	@Override
	public void onResume() {
		//getActivity().setTitle(R.string.app_name);
		//getActivity().getActionBar().setTitle(R.string.app_name);
		super.onResume();
	}

	public void sendInfoBroadcast(int action_id, String message) {
		Intent intent = new Intent(TDClientService.INFO_ACTION);
		intent.putExtra(ParamsAndConstants.TYPE, action_id);
		intent.putExtra(ParamsAndConstants.MSG_TEXT, message);
		activity.sendBroadcast(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View arg1, int position,
			long arg3) {
		Employee employee = (Employee) list.getItemAtPosition(position);

		if (employee != null) {
			try {
				Message msg = new Message();
				msg.arg1 = ParamsAndConstants.ID_ACTION_SET_HISTORY_ADR;
				Bundle bnd = new Bundle();
				Toast.makeText(activity, employee.getName(),
						Toast.LENGTH_LONG).show();
				bnd.putString("msg_text", employee.getName());
				msg.setData(bnd);
				((MainActivity)activity).handle.sendMessage(msg);
			} catch (Exception ex) {
				//showMyMsg("Ошибка onItemClick: " + ex);
				Toast.makeText(activity, "Ошибка onItemClick: " + ex,
						Toast.LENGTH_LONG).show();
			}
			//sendInfoBroadcast(ParamsAndConstants.ID_ACTION_SET_HISTORY_ADR, employee.getName());
			//Bundle arguments = new Bundle();
			//arguments.putParcelable("selectedEmployee", employee);
			//CustomEmpDialogFragment customEmpDialogFragment = new CustomEmpDialogFragment();
			//customEmpDialogFragment.setArguments(arguments);
			//customEmpDialogFragment.show(getFragmentManager(),
			//		CustomEmpDialogFragment.ARG_ITEM_ID);
		}
	}

	/*@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long arg3) {
		Employee employee = (Employee) parent.getItemAtPosition(position);

		// Use AsyncTask to delete from database
		employeeDAO.delete(employee);
		employeeListAdapter.remove(employee);
		return true;
	}*/

	public class GetEmpTask extends AsyncTask<Void, Void, ArrayList<Employee>> {

		private final WeakReference<Activity> activityWeakRef;

		public GetEmpTask(Activity context) {
			this.activityWeakRef = new WeakReference<Activity>(context);
		}

		@Override
		protected ArrayList<Employee> doInBackground(Void... arg0) {
			ArrayList<Employee> employeeList = employeeDAO.getEmployees();
			return employeeList;
		}

		@Override
		protected void onPostExecute(ArrayList<Employee> empList) {
			if (activityWeakRef.get() != null
					&& !activityWeakRef.get().isFinishing()) {
				Log.d("employees", empList.toString());
				employees = empList;
				if (empList != null) {
					if (empList.size() != 0) {
						employeeListAdapter = new EmpListAdapter(activity,
								empList);
						employeeListView.setAdapter(employeeListAdapter);
					} else {
						//Toast.makeText(activity, "No Employee Records",
						//		Toast.LENGTH_LONG).show();
					}
				}

			}
		}
	}

	/*
	 * This method is invoked from MainActivity onFinishDialog() method. It is
	 * called from CustomEmpDialogFragment when an employee record is updated.
	 * This is used for communicating between fragments.
	 */
	public void updateView() {
		task = new GetEmpTask(activity);
		task.execute((Void) null);
	}
}
