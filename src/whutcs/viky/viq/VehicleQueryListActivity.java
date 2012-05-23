/**
 * 
 */
package whutcs.viky.viq;

import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_SELECTION;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Show a list of historical queries, including the vehicle image taken or
 * selected, the licence , the owner's name and phone if can be retrieved from
 * the database, and the time and place the query happens.</br> The implemention
 * of this class is similar to VehicleInfoListActivity.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleQueryListActivity extends ViqShakeBaseListActicity {
	private static final String TAG = "VehicleInfoListActivity";
	private AdapterContextMenuInfo mContextMenuInfo;

	@Override
	protected void setDefaultTitle() {
		setDefaultTitle(getString(R.string.vehicle_query_list));
	}

	@Override
	protected void setTableName() {
		setTableName(VIEW_QUERY_INFO);
	}

	@Override
	protected void setFrom() {
		setFrom(VIEW_QUERY_INFO_COLUMNS);
	}

	@Override
	protected void setTo() {
		setTo(new int[] { R.id.rowid, R.id.licence, R.id.name, R.id.phone,
				R.id.time, R.id.place, R.id.note, R.drawable.vehicle });
	}

	@Override
	protected void setSelection() {
		setSelection(VIEW_QUERY_INFO_SELECTION);
	}

	@Override
	protected void setColumnPhoto() {
		setColumnPhoto(VIEW_QUERY_INFO_COLUMN_PHOTO);
	}

	@Override
	protected void setListItemId() {
		setListItemId(R.layout.vehicle_query_list_item);
	}

	@Override
	protected void setForwardButtonListener() {
		setForwardButtonListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(VehicleQueryListActivity.this,
						VehicleInfoListActivity.class));
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_query_list_context_menu, menu);

		mContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);

		if (item.getItemId() == R.id.menu_copy) {
			return result;
		}

		long id = mContextMenuInfo.id;
		int position = mContextMenuInfo.position;

		// Retrieve the cursor (row) that defines this item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String licence = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_LICENCE);
		String name = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NAME);
		String phone = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PHONE);
		String time = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_TIME);
		String place = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PLACE);
		String note = cursor
				.getString(ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NOTE);

		StringBuilder builder = new StringBuilder();
		String comma = getString(R.string.comma) + " ";
		builder.append(licence).append(comma).append(name).append(comma)
				.append(phone).append(comma).append(time).append(comma)
				.append(place).append(comma).append(note);
		String all = builder.toString();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		switch (item.getItemId()) {
		case R.id.menu_view:
			startActivity(new Intent(this, VehicleItemViewActivity.class)
					.putExtra("licence", licence));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra("_id", id));
			break;
		case R.id.menu_delete:
			deleteItem(id, licence);
			break;
		case R.id.menu_call_owner:
			startActivity(new Intent(Intent.ACTION_CALL).setData(Uri
					.parse("tel:" + phone)));
			break;

		// Sub menu items of menu item menu_copy:
		case R.id.menu_copy_licence_number:
			clipboard.setText(licence);
			break;
		case R.id.menu_copy_owner_name:
			clipboard.setText(name);
			break;
		case R.id.menu_copy_owner_phone_number:
			clipboard.setText(phone);
			break;
		case R.id.menu_copy_time:
			clipboard.setText(time);
			break;
		case R.id.menu_copy_place:
			clipboard.setText(place);
			break;
		case R.id.menu_copy_note:
			clipboard.setText(note);
			break;
		case R.id.menu_copy_all:
			clipboard.setText(all);
			break;
		case R.id.menu_sms_vehicle_query:
			startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
					.putExtra("sms_body", all));
			break;
		default:
			break;
		}

		Log.v(TAG, clipboard.getText().toString());
		return result;
	}

}