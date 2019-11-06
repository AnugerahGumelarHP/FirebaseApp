package com.battistradadeveloper.firebaseapp;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

	//Firebase
	FirebaseAuth firebaseAuth;
	FirebaseUser user;
	FirebaseDatabase firebaseDatabase;
	DatabaseReference databaseReference;
	//storage
	StorageReference storageReference;
	//path where images of user profile and cover will be stored
	String storagePath = "Users_Profile_Cover_Imgs/";

	//views from xml
	ImageView avatarIv, coverIv;
	TextView nameTv, emailTv, workTv;
	FloatingActionButton fab;
	CardView cvQuestionare, cvLogout;

	//progress Dialog
	ProgressDialog pd;

	//permissions constants
	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_GALLERY_CODE = 300;
	private static final int IMAGE_PICK_CAMERA_CODE = 400;
	//arrays of permissions to be requested
	String cameraPermissions[];
	String storagePermissions[];

	//uri of pick image
	Uri image_uri;

	//for checking profile or cover photo
	String profileOrCoverPhoto;

	public ProfileFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_profile, container, false);

		//init firebase
		firebaseAuth = FirebaseAuth.getInstance();
		user = firebaseAuth.getCurrentUser();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("Users");
		storageReference = getInstance().getReference(); //firebase storage reference

		//init views
		avatarIv = view.findViewById(R.id.avatarIv);
		coverIv = view.findViewById(R.id.coverIv);
		nameTv = view.findViewById(R.id.nameTv);
		emailTv = view.findViewById(R.id.emailTv);
		workTv = view.findViewById(R.id.workTv);
		cvQuestionare = view.findViewById(R.id.cv_Questionnaire);
		cvQuestionare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent questioner = new Intent(getActivity(), Questionnaire.class);
				startActivity(questioner);
			}
		});
		cvLogout = view.findViewById(R.id.Logout);
		cvLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				firebaseAuth.signOut();
				checkUserStatus();
			}
		});
		fab = view.findViewById(R.id.fab);

		//init arrays of permissions
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

		//init progress dialog
		pd = new ProgressDialog(getActivity());

		/* We have to get info of currently signed in user. We can get it using user's email or uid
		   I'm gonna retrieve user detail using email*/
		/* By using orderByChild query will show the detail from a node
		whose key named email has value equal to currently signed in email.
		It will search all nodes, where the key matches it will get its detail*/
		Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

				//check until required data get
				for (DataSnapshot ds : dataSnapshot.getChildren()){
					//get data
					String name = ""+ ds.child("name").getValue();
					String email = ""+ ds.child("email").getValue();
					String work = ""+ ds.child("company").getValue();
					String image = ""+ ds.child("image").getValue();
					String cover = ""+ ds.child("cover").getValue();

					//set data
					nameTv.setText(name);
					emailTv.setText(email);
					workTv.setText(work);
					try {
						//if image is received then set
						Picasso.get().load(image).into(avatarIv);
					} catch (Exception e){
						//if there is any exception while getting image then set default
						Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
					}

					try {
						//if image is received then set
						Picasso.get().load(cover).into(coverIv);
					} catch (Exception e){
						//if there is any exception while getting image then set default
					}
				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		//fab button click
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEditProfileDialog();
			}
		});

		return view;
	}

	private boolean checkStoragePermission(){
		//check if storage permission is enabled or not
		//return true if enabled
		//return false if not enabled
		boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== (PackageManager.PERMISSION_GRANTED);
		return result;
	}
	private void requestStoragePermission(){
		//request runtime storage permission
		requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
	}

	private boolean checkCameraPermission(){
		//check if storage permission is enabled or not
		//return true if enabled
		//return false if not enabled
		boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
				== (PackageManager.PERMISSION_GRANTED);

		boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== (PackageManager.PERMISSION_GRANTED);
		return result && result1;
	}
	private void requestCameraPermission(){
		//request runtime storage permission
		requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
	}

	private void showEditProfileDialog() {
		/* Show dialog containing options
		   1) Edit Profile Picture
		   2) Edit Cover Photo
		   3) Edit Name
		   4) Edit Work
		 */

		//options to show in dialog
		String options[] = {"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Work"};
		//alert dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set title
		builder.setTitle("Choose Action");
		//set items to dialog
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//handle dialog item clicks
				if (which == 0){
					//Edit Profile Clicked
					pd.setMessage("Updating Profile Picture");
					profileOrCoverPhoto = "image"; //i.e. changing profile picture, make sure to asssign same value
					showImagePicDialog();
				}
				else if (which == 1){
					//Edit Cover CLicked
					pd.setMessage("Updating Cover Photo");
					profileOrCoverPhoto = "cover"; //i.e. changing cover photo, make sure to asssign same value
					showImagePicDialog();
				}
				else if (which == 2){
					//Edit Name Clicked
					pd.setMessage("Updating Name");
					//calling method and pass key "name" as a parameter to update it's value in database
					showNameWorkUpdateDialog("name");
				}
				else if (which == 3){
					//Edit Work Clicked
					pd.setMessage("Updating Work");
					showNameWorkUpdateDialog("company");
				}
			}
		});
		//create and show dialog
		builder.create().show();
	}

	private void showNameWorkUpdateDialog(final String key) {
		/*parameter "key" will contain value:
		    either "name" which is key in user's database which is used to update user's name
		    or     "work" which is key in user's database which
		 */

		//custom dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Update "+ key); //e.g. Update name or Update work
		//set layout of dialog
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(10,10,10,10);
		//add edit text
		final EditText editText = new EditText(getActivity());
		editText.setHint("Enter "+key); //hint e.g. Edit name or Edit Phone
		linearLayout.addView(editText);

		builder.setView(linearLayout);

		//add buttons in dialog to update
		builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//input text from edit text
				String value = editText.getText().toString().trim();
				//validate if user has entered something or not
				if (!TextUtils.isEmpty(value)){
					pd.show();
					HashMap<String, Object> result = new HashMap<>();
					result.put(key, value);

					databaseReference.child(user.getUid()).updateChildren(result)
							.addOnSuccessListener(new OnSuccessListener<Void>() {
								@Override
								public void onSuccess(Void aVoid) {
									//updated, dismiss progress
									pd.dismiss();
									Toast.makeText(getActivity(),"Updated...", Toast.LENGTH_SHORT).show();
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									//failed, dismiss progress, get and show error message
									pd.dismiss();
									Toast.makeText(getActivity(),""+e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							});
				}
				else{
					Toast.makeText(getActivity(),"Please enter "+key,Toast.LENGTH_SHORT).show();
				}
			}
		});
		//add buttons in dialog to cancel
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		//create and show dialog
		builder.create().show();
	}

	private void showImagePicDialog() {
		//show dialog containing options camera and gallery to pick the image
		String options[] = {"Camera","Gallery"};
		//alert dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//set title
		builder.setTitle("Pick Image From");
		//set items to dialog
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//handle dialog item clicks
				if (which == 0){
					//Camera Clicked
					if (!checkCameraPermission()){
						requestCameraPermission();
					}
					else {
						pickFromCamera();
					}
				}
				else if (which == 1){
					//Gallery Clicked
					if (!checkStoragePermission()){
						requestStoragePermission();
					}
					else {
						pickFromGallery();
					}
				}
			}
		});
		//create and show dialog
		builder.create().show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		/*This method called when user press Allow or Deny from permission request dialog
		  here we will handle permission casses (allowed & denied)
		 */
		switch (requestCode){
			case CAMERA_REQUEST_CODE:{
				//picking from camera, first check if camera and storage permissions allowed or not
				if (grantResults.length >0){
					boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (cameraAccepted && writeStorageAccepted){
						//permissions enabled
						pickFromCamera();
					}
					else {
						//permissions denied
						Toast.makeText(getActivity(), "Please enable camera & storage permission",Toast.LENGTH_LONG).show();
					}
				}
			}
			break;
			case STORAGE_REQUEST_CODE:{
				//picking from gallery, first check if storage permissions allowed or not
				if (grantResults.length >0){
					boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (writeStorageAccepted){
						//permissions enabled
						pickFromGallery();
					}
					else {
						//permissions denied
						Toast.makeText(getActivity(), "Please enable storage permission",Toast.LENGTH_LONG).show();
					}
				}
			}
			break;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		/*This method will be called after picking image from camera or gallery */
		if (resultCode == RESULT_OK){
			if (requestCode == IMAGE_PICK_GALLERY_CODE){
				//image is picked from gallery, get uri of image
				image_uri = data.getData();

				uploadProfileCoverPhoto(image_uri);
			}
			if (requestCode == IMAGE_PICK_CAMERA_CODE){
				//image is picked from camera, get uri of image

				uploadProfileCoverPhoto(image_uri);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uploadProfileCoverPhoto(Uri uri) {
		//show progress dialog
		pd.show();

		/*Instead of creating separate function for profile picture and cover photo
		 *i'm doing work for both in same function
		 *
		 * To add check ill add a string variable and assign it value "image" when user clicks
		 * "Edit Profile Pict", and assign it value "cover" when user clicks "Edit Cover Photo"
		 * Here: image is the key in each user containing url of user's profile picture
		 *       cover is the key in each user containing url of user's cover photo
		 */

		/*The parameter "image_uri" contains the uri of image picked either from camera or gallery
		 *We will use UID of the currently signed in user as name of the image so there will be only one image
		 *profile and one image for cover for each user
		 */

		//path and name of image to be stored in firebase storage
		//e.g. Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
		//e.g. Users_Profile_Cover_Imgs/cover_c123n4567g89.jpg
		String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid();

		StorageReference storageReference2nd = storageReference.child(filePathAndName);
		storageReference2nd.putFile(uri)
				.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
						//image is uploaded to storage, now get it's url and store in user's database
						Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
						while (!uriTask.isSuccessful());
						Uri downloadUri = uriTask.getResult();

						//check if image is uploaded or not and url is received
						if (uriTask.isSuccessful()){
							//image uploaded
							//add/update url in user's database
							HashMap<String, Object> results = new HashMap<>();
							/*First parameter is profileOrCoverPhoto that has value "image" or "cover"
							  which are key in user's database where url of image will be saved in one
							  of them
							  Second parameter contains the url of the image stored in firebase storage, this
							  url will be saved as value against key "image" or "cover"*/
							results.put(profileOrCoverPhoto, downloadUri.toString());

							databaseReference.child(user.getUid()).updateChildren(results)
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											//url in database of user is added succesfully
											//dismiss progress bar
											pd.dismiss();
											Toast.makeText(getActivity(),"Image Uploaded...",Toast.LENGTH_LONG).show();
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											// error adding url in database of user
											//dismiss progress bar
											pd.dismiss();
											Toast.makeText(getActivity(),"Error Updating Image...",Toast.LENGTH_SHORT).show();
										}
									});
						}
						else{
							//error
							pd.dismiss();
							Toast.makeText(getActivity(),"Some error occured",Toast.LENGTH_LONG).show();
						}

					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						//there were some error(s), get and show error message, dismiss progress dialog
						pd.dismiss();
						Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
	}

	private void pickFromCamera() {
		//Intent of picking image from device camera
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
		values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
		//put image uri
		image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		//intent to start camera
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
		startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
	}

	private void pickFromGallery() {
		//pick from gallery
		Intent galleryIntent = new Intent(Intent.ACTION_PICK);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
	}

	private void checkUserStatus(){
		//get current user
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null){
			//user is signed in stay away
			//set email of logged in user
			//mProfileTv.setText(user.getEmail());
		} else{
			//user not signed in, go to main activity
			startActivity(new Intent(getActivity(),MainActivity.class));
		}
	}
}