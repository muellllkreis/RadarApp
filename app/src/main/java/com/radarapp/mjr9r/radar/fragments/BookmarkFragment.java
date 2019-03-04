package com.radarapp.mjr9r.radar.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;
import com.radarapp.mjr9r.radar.Database.MessageDao;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class BookmarkFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private DropMessageRecyclerViewAdapter adapter;

    private MapsActivity mainActivity;
    private int sortingOption = 0;

    public void setOnListFragmentInteractionListener(Activity activity) {
        mListener = (OnListFragmentInteractionListener) activity;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookmarkFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static BookmarkFragment newInstance(int columnCount) {
        BookmarkFragment fragment = new BookmarkFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        mainActivity = (MapsActivity) this.getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actiobar_listview, menu);
        mainActivity.getSupportActionBar().setTitle(R.string.actionbar_title_listview);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final ArrayList selectedItems = mainActivity.getSelectedItems();
        List<DropMessage> dropMessages = mainActivity.getDropMessages();
        final boolean[] checkedItems = mainActivity.getCheckedItems();

        switch (item.getItemId()) {
            case R.id.action_filter: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle(R.string.filter_dialog_title)
                        // Specify the list array, the items to be selected by default (null for none),
                        // and the listener through which to receive callbacks when items are selected
                        .setMultiChoiceItems(R.array.filters, checkedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which,
                                                        boolean isChecked) {
                                        if (isChecked) {
                                            // If the user checked the item, add it to the selected items
                                            selectedItems.add(Filter.values()[which].getName());
                                            checkedItems[which] = true;
                                        } else if (selectedItems.contains(Filter.values()[which].getName())) {
                                            // Else, if the item is already in the array, remove it
                                            selectedItems.remove(Filter.values()[which].getName());
                                            checkedItems[which] = false;
                                        }
                                    }
                                })
                        // Set the action buttons
                        .setPositiveButton(R.string.filter_dialog_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the selectedItems results somewhere
                                // or return them to the component that opened the dialog
                                refreshAdapter();
                            }
                        })
                        .setNegativeButton(R.string.filter_dialog_negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
            case R.id.action_sort: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle(R.string.sort_dialog_title)
                        // Specify the list array, the items to be selected by default (null for none),
                        // and the listener through which to receive callbacks when items are selected
                        .setSingleChoiceItems(R.array.sortOptions, sortingOption,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sortingOption = which;
                                    }
                                })
                        .setPositiveButton(R.string.sort_dialog_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                refreshAdapter();
                                return;
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
            case R.id.action_settings: {
                mainActivity.openSettings(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_list, container, false);

//        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            Log.v("LISTVIEW", "SETTING ADAPTER");
            adapter = new DropMessageRecyclerViewAdapter(getSavedMessages(), mListener);
            recyclerView.setAdapter(adapter);
        }


        Log.v("LISTVIEW", "RETURNING ADAPTER");
        return view;
    }

    public void refreshAdapter() {
        //this.adapter.setmValues(getSavedMessages());
        this.adapter.updateAdapter(this.getSavedMessages());
        this.sortBy(sortingOption);
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mListener = (OnListFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            Log.v("LISTCLICK", e.toString());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DropMessage message);
    }

    public List<DropMessage> getSavedMessages() {
        final ArrayList selectedItems = mainActivity.getSelectedItems();
        List<DropMessage> dropMessages = mainActivity.getDropMessages();
        final boolean[] checkedItems = mainActivity.getCheckedItems();

        dropMessages.clear();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        MainFragment mainFragment = (MainFragment) fm.findFragmentByTag("MAP_FRAGMENT");
        for(Marker m : mainFragment.getMarkers()) {
            DropMessage dm = (DropMessage) m.getTag();
            Log.v("LISTVIEW", "CURRENT FILTER " + dm.getFilter().getName());
            if(selectedItems.isEmpty()) {
                dropMessages.add(dm);
            }
            else if(selectedItems.contains(dm.getFilter().getName())) {
                dropMessages.add(dm);
            }
            else {
                continue;
            }

        }
        return dropMessages;
    }

    public void sortBy(int criteria) {
        switch(criteria) {
            case 0: {
                Collections.sort(mainActivity.getDropMessages(), new Comparator<DropMessage>() {
                    @Override
                    public int compare(DropMessage lhs, DropMessage rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        return lhs.getUnixTime() > rhs.getUnixTime() ? -1 : (lhs.getUnixTime() < rhs.getUnixTime() ) ? 1 : 0;
                    }
                });
                break;
            }
            case 1: {
                Collections.sort(mainActivity.getDropMessages(), new Comparator<DropMessage>() {
                    @Override
                    public int compare(DropMessage lhs, DropMessage rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        return lhs.getFilter().getIconID() > rhs.getFilter().getIconID() ? -1 : (lhs.getFilter().getIconID() < rhs.getFilter().getIconID() ) ? 1 : 0;
                    }
                });
                break;
            }
            default: {
                break;
            }
        }
    }

}

