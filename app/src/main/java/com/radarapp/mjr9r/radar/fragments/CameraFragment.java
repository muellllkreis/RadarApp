package com.radarapp.mjr9r.radar.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.Files;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment implements SurfaceHolder.Callback {

    private MapsActivity mainActivity;
    Fragment caller;
    public Fragment getCaller() {
        return caller;
    }
    public void setCaller(Fragment caller) {
        this.caller = caller;
    }

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera mCamera;

    private OnFragmentInteractionListener mListener;

    private View inflatedLayout;
    private View triggerBtn;
    private FloatingActionButton sendFab;
    private EditText quickdropCameraContent;
    private TextView tooltip;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            inflatedLayout.getWindowVisibleDisplayFrame(r);

            int heightDiff = inflatedLayout.getBottom() - r.bottom;

            int suggestionsBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,75,getActivity().getResources().getDisplayMetrics());

            if (inflatedLayout.getRootView().getHeight() - (r.bottom - r.top) > 500) {
                //Log.d("keyboardStatus","opened");
                sendFab.setTranslationY(-(heightDiff + suggestionsBarHeight));
            } else {
                sendFab.setTranslationY(0);
            }
        }
    };

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainActivity = (MapsActivity) this.getActivity();
        this.camera = mainActivity.getCamera();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actiobar_settings, menu);
        mainActivity.getSupportActionBar().setTitle("");
        mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mainActivity.getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v("HOMEBUTTON", "BUTTON PRESSED");
                inflatedLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
                mainActivity.closeCamera(getCaller());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedLayout =  inflater.inflate(R.layout.fragment_camera, container, false);

        triggerBtn = inflatedLayout.findViewById(R.id.photo_trigger);
        sendFab = inflatedLayout.findViewById(R.id.camera_sendphoto);
        tooltip = inflatedLayout.findViewById(R.id.tooltip);

        quickdropCameraContent = inflatedLayout.findViewById(R.id.quickdrop_photo_content);
        sendFab.setVisibility(View.GONE);

        this.surfaceView = inflatedLayout.findViewById(R.id.surfaceView);
        this.surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        inflatedLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        return inflatedLayout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Camera.Parameters params = camera.getParameters();
            setCameraDisplayOrientation(mainActivity, 0, camera);

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            triggerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(final byte[] data, Camera camera) {
                            Log.v("CAMERALOG", "ONPICTURETAKEN");
                            Log.v("CAMERALOG", "Getting output media file");

                            sendFab.setVisibility(View.VISIBLE);
                            triggerBtn.setVisibility(View.GONE);
                            tooltip.setVisibility(View.GONE);

                            sendFab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                    progressDialog.setTitle("Uploading...");
                                    progressDialog.show();

                                    final File pictureFile = getOutputMediaFile();
                                    final StorageReference storageRef = mainActivity.getRemoteDb().getReference().child("images/" + pictureFile.getName().toString());

                                    if (pictureFile == null) {
                                        Log.v("CAMERALOG", "Error creating output file");
                                        return;
                                    }

                                    final UploadTask uploadTask = storageRef.putBytes(data);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast toast = Toast.makeText(getActivity(), "Unable to post, check connection.", Toast.LENGTH_SHORT);
                                            toast.show();
                                            progressDialog.dismiss();
                                            cleanExit();
                                        }
                                    });
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Log.v("UPLOADLOG", "Upload has succeded");
                                            try {
                                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        mainActivity.submitPhoto(quickdropCameraContent.getText().toString(), uri);
                                                        Log.v("UPLOADLOG", "DOWNLOADLINK: " + uri.toString());
                                                    }
                                                });
                                                FileOutputStream fos = new FileOutputStream(pictureFile);
                                                fos.write(data);
                                                fos.close();
                                                Log.v("UPLOADLOG", "Written to Storage");
                                                progressDialog.dismiss();
                                                Log.v("CAMERALOG", "File created under " + pictureFile.getAbsolutePath());
                                                MediaScannerConnection.scanFile(getContext(), new String[]{pictureFile.getPath()}, null, null);
                                            } catch (FileNotFoundException e) {
                                                Log.v(TAG, e.getMessage());
                                            } catch (IOException e) {
                                                Log.v(TAG, e.getMessage());
                                            }
                                            cleanExit();
                                        }
                                    });
                                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                                    .getTotalByteCount());
                                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });

        } catch (IOException e) {
            Log.v("CAMERALOG", "IOEXCEPETION");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

        public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
        }

    private static File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        else {
            File folder_gui = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Radar");
            if (!folder_gui.exists()) {
                Log.v(TAG, "Creating folder: " + folder_gui.getAbsolutePath());
                folder_gui.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outFile = new File(folder_gui.getPath() + File.separator + "RDR_" + timeStamp + ".jpg");
            Log.v(TAG, "Returning file: " + outFile.getAbsolutePath());
            return outFile;
        }
    }

    private void cleanExit() {
        inflatedLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        mainActivity.closeCamera(getCaller());
    }
}
