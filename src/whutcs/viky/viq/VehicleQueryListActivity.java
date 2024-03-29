/**
 * 
 */
package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_ID;
import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_LICENCE;
import static whutcs.viky.viq.ViqCommonUtilities.getRelativeTime;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NAME;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PHONE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_PLACE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_COLUMN_TIME;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.VIEW_QUERY_INFO_SELECTION;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.getSelectiionArgs;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows a list of historical queries, including the vehicle image taken or
 * selected, the licence , the owner's name and phone if can be retrieved from
 * the database, and the time and place the query happens.</br> The implemention
 * of this class is similar to VehicleInfoListActivity.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleQueryListActivity extends ViqBaseShakeableListActivity {
	private static final String TAG = "VehicleQueryListActivity";

	private AdapterContextMenuInfo mContextMenuInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mDefaultTitle = getString(R.string.vehicle_query_list);
		mForwardButtonText = getString(R.string.vehicle_info_list);
		mForwardClass = VehicleInfoListActivity.class;
		mWriteableTableName = TABLE_QUERY;

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		mContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
		long id = mContextMenuInfo.id;

		int position = mContextMenuInfo.position;
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String licence = cursor.getString(VIEW_QUERY_INFO_COLUMN_LICENCE);
		String time = cursor.getString(VIEW_QUERY_INFO_COLUMN_TIME);
		String relativeTime = getRelativeTime(this, time);

		menu.setHeaderTitle(id + ". " + licence + ", " + relativeTime);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_query_list_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);

		if (item.getItemId() == R.id.menu_copy) {
			return result;
		}

		long id = mContextMenuInfo.id;
		int position = mContextMenuInfo.position;

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String licence = cursor.getString(VIEW_QUERY_INFO_COLUMN_LICENCE);
		String name = cursor.getString(VIEW_QUERY_INFO_COLUMN_NAME);
		String phone = cursor.getString(VIEW_QUERY_INFO_COLUMN_PHONE);
		String time = cursor.getString(VIEW_QUERY_INFO_COLUMN_TIME);
		String place = cursor.getString(VIEW_QUERY_INFO_COLUMN_PLACE);
		String note = cursor.getString(VIEW_QUERY_INFO_COLUMN_NOTE);

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
					.putExtra(EXTRA_LICENCE, licence));
			break;
		case R.id.menu_edit:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra(EXTRA_ID, id));
			break;
		case R.id.menu_delete:
			deleteItem(id, licence);
			break;
		case R.id.menu_call_owner:
			if (phone == null || phone.length() == 0) {
				Toast.makeText(this, getString(R.string.no_phone_found),
						Toast.LENGTH_SHORT).show();
			} else {
				startActivity(new Intent(Intent.ACTION_CALL).setData(Uri
						.parse("tel:" + phone)));
			}
			break;
		case R.id.menu_sms_all:
			startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
					.putExtra("sms_body", all));
			break;
		case R.id.menu_fast_check:
			startActivity(new Intent(this, VehicleQueryEditActivity.class)
					.putExtra(EXTRA_LICENCE, licence));
			break;

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
		default:
			break;
		}

		Log.v(TAG, clipboard.getText().toString());
		return result;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Retrieve the cursor (row) that defines the clicked item.
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		// Get the licence field of the clicked item
		String licence = cursor.getString(VIEW_QUERY_INFO_COLUMN_LICENCE);

		startActivity(new Intent(this, VehicleItemViewActivity.class).putExtra(
				EXTRA_LICENCE, licence));

	}

	@Override
	protected void refreshListView() {
		super.refreshListView();

		String filter = getFilter();
		// Should never be closed explicitly until onDestroy().
		Cursor cursor;

		SQLiteDatabase database = mHelper.getReadableDatabase();
		// Get the cursor.
		if (filter.length() == 0) {
			cursor = database.query(VIEW_QUERY_INFO, VIEW_QUERY_INFO_COLUMNS,
					null, null, null, null, "_id DESC");
		} else {
			String[] selectionArgs = getSelectiionArgs(getFilter(),
					VIEW_QUERY_INFO_COLUMNS.length);
			cursor = database.query(VIEW_QUERY_INFO, VIEW_QUERY_INFO_COLUMNS,
					VIEW_QUERY_INFO_SELECTION, selectionArgs, null, null,
					"_id DESC");
		}
		// Bind or rebind the cursor to the list adapter.
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
		if (adapter == null) {
			adapter = new SimpleCursorAdapter(this,
					R.layout.vehicle_query_list_item, cursor,
					VIEW_QUERY_INFO_COLUMNS, new int[] { R.id.rowid,
							R.id.licence, R.id.name, R.id.phone, R.id.time,
							R.id.place, R.id.note, R.id.vehicle });
			adapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Cursor cursor,
						int columnIndex) {
					boolean result = false;
					if (columnIndex == VIEW_QUERY_INFO_COLUMN_PHOTO) {
						final ImageView imageView = (ImageView) view;
						final String imageName = cursor.getString(columnIndex);
						if (imageName != null) {
							// set image in new thread
							ViqCachedImageFetcher fetchCacher = new ViqCachedImageFetcher(
									imageName, imageView);
							fetchCacher.run();
							result = true;
						}
					} else if (columnIndex == VIEW_QUERY_INFO_COLUMN_TIME) {
						TextView textView = (TextView) view;
						String time = cursor.getString(columnIndex);
						String relativeTime = getRelativeTime(
								VehicleQueryListActivity.this, time);
						textView.setText(relativeTime);
						result = true;
					}
					return result;
				}
			});

			setListAdapter(adapter);
		} else {
			// Will close the previous cursor.
			adapter.changeCursor(cursor);
			adapter.notifyDataSetChanged();
		}
		database.close();

		// Update mMatchesView to show the latest record count.
		mMatchesView.setText("" + cursor.getCount());

	}
}
