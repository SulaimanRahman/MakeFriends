package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.StructuredQuery;
import com.google.gson.internal.$Gson$Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private FirebaseFirestore db;
    private StorageReference storageReference;
    private String TAG;

    private boolean collectionExists;

    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        TAG = "DatabaseManager";

        collectionExists = false;

    }

    public void deleteCollection(String collectionPath) {
        final CollectionReference collectionReference = db.collection(collectionPath);

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int totalNumberOfDocuments = queryDocumentSnapshots.size();

                for (int i = 0 ; i < totalNumberOfDocuments; i++) {
                    DocumentReference documentReference = collectionReference.document(queryDocumentSnapshots.getDocuments().get(i).getId());
                    documentReference.delete();
                }
                Log.d(TAG, "Collection Deleted Successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Deleting Collection Failed: " + e);
            }
        });
    }

    public void findOutIfThisCollectionExists(String collectionPath, final FirebaseCallback firebaseCallback) {
        CollectionReference collectionReference = db.collection(collectionPath);

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                try {
                    queryDocumentSnapshots.getDocuments().get(0);
                    collectionExists = true;
                } catch (Exception e) {
                    collectionExists = false;
                }
                Log.d(TAG, "Checking if the collection exists completed");
                firebaseCallback.onCallback(collectionExists);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to check if the collection Exists: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void getQueryDocumentSnapshot(String collectionPath, final FirebaseCallback firebaseCallback) {
        CollectionReference collectionReference = db.collection(collectionPath);
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG, "Getting query document snapshots successful");
                firebaseCallback.onCallback(queryDocumentSnapshots);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting query document snapshots failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void getAllDocumentsNameInArrayListFromCollection(String collectionPath, final FirebaseCallback firebaseCallback) {
        CollectionReference collectionReference = db.collection(collectionPath);
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList allDocuments = new ArrayList();
                int numberOfDocuments = queryDocumentSnapshots.size();
                for (int i = 0; i < numberOfDocuments; i++) {
                    String documentName = queryDocumentSnapshots.getDocuments().get(i).getId();
                    allDocuments.add(documentName);
                }
                Log.d(TAG, "Getting all documents name in array list from collection was successful");
                firebaseCallback.onCallback(allDocuments);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting all documents name in array list from collection failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void getTotalNumberOfDocumentsFromCollection(String collectionPath, final FirebaseCallback firebaseCallback) {
        CollectionReference collectionReference = db.collection(collectionPath);
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int totalNumberOfDocuments = queryDocumentSnapshots.getDocuments().size();
                Log.d(TAG, "Getting total number of documents from collection was successful");
                firebaseCallback.onCallback(totalNumberOfDocuments);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting total number of documents from collection failed: " + e );
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void getAllDocumentsInArrayListFromCollection(String collectionPath, final FirebaseCallback firebaseCallback) {
        CollectionReference collectionReference = db.collection(collectionPath);
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList allDocuments = new ArrayList();
                allDocuments.addAll(queryDocumentSnapshots.getDocuments());
                Log.d(TAG, "Getting all documents in array list was successful");
                firebaseCallback.onCallback(allDocuments);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting all documents in array failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void createDocument(String documentPath, String firstFieldKey, Object fieldValue) {
        DocumentReference documentReference = db.document(documentPath);

        HashMap<String, Object> data = new HashMap<>();
        data.put(firstFieldKey, fieldValue);

        documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document created successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Creating document failed");
            }
        });
    }

    public void checkIfThisDocumentExists(String documentPath, final FirebaseCallback firebaseCallback) {
        DocumentReference documentReference = db.document(documentPath);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Boolean existence = documentSnapshot.exists();
                Log.d(TAG, "Successfully checked if the document exists");
                firebaseCallback.onCallback(existence);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to check if the document exists");
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void duplicateDocumentWithNewName(final String collectionPath, String documentCurrentName, final String documentNewName) {
        DocumentReference documentReference = db.collection(collectionPath).document(documentCurrentName);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                HashMap<Object, Object> data = new HashMap<>();
                data.putAll(documentSnapshot.getData());

                DocumentReference documentReference1 = db.collection(collectionPath).document(documentNewName);
                documentReference1.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully added all the data from old document to the new document");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to add data to the new Document: " + e);
                    }
                });
                Log.d(TAG, "Successfully duplicated the document");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to duplicate the document");
            }
        });
    }

    public void deleteDocument(String documentPath) {
        DocumentReference documentReference = db.document(documentPath);

        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document successfully deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Deleting the document failed");
            }
        });
    }

    public void renameTheDocument() {

    }

    public void getDocumentSnapshot(String documentPath, final FirebaseCallback firebaseCallback) {
        DocumentReference documentReference = db.document(documentPath);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "Getting document snapshot successful");
                firebaseCallback.onCallback(documentSnapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting document snapshot failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void createNewField(String documentPath, String fieldKey, Object fieldValue) {
        DocumentReference documentReference = db.document(documentPath);
        HashMap<String, Object> data = new HashMap<>();
        data.put(fieldKey, fieldValue);
        documentReference.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully created the new field");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to create the new field");
            }
        });;
    }

    public void getFieldValue(String documentPath, final String fieldName, final FirebaseCallback firebaseCallback) {
        DocumentReference documentReference = db.document(documentPath);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Object fieldValue = documentSnapshot.get(fieldName);
                Log.d(TAG, "Getting field value successful");
                firebaseCallback.onCallback(fieldValue);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting field value failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void getAllTheFieldsKeyInArrayListFromDocument(String documentPath, final FirebaseCallback firebaseCallback) {
        DocumentReference documentReference = db.document(documentPath);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                HashMap<String, Object> data = new HashMap<>();
                data.putAll(documentSnapshot.getData());

                ArrayList fieldsName = new ArrayList();
                fieldsName.addAll(Arrays.asList(data.keySet().toArray()));

                Log.d(TAG, "Getting all the fields name in array list from a document successful");
                firebaseCallback.onCallback(fieldsName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting all the fields name in array list from a document failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void getAllDocumentDataInHashMap(final String documentPath, final FirebaseCallback firebaseCallback) {
        DocumentReference documentReference = db.document(documentPath);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    HashMap<String, Object> data = new HashMap();
                    data.putAll(documentSnapshot.getData());
                    Log.d(TAG, "Getting all the document data in Hash Map successful");
                    firebaseCallback.onCallback(data);
                } else {
                    Log.d(TAG, documentPath + " doese not exists");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Getting all the document data in Hash Map failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void checkIfThisFieldExists(String documentPath, final String fieldName, final FirebaseCallback firebaseCallback) {
        DocumentReference documentReference = db.document(documentPath);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                boolean fieldExists;
                if (documentSnapshot.get(fieldName) != null) {
                    fieldExists = true;
                } else {
                    fieldExists = false;
                }

                Log.d(TAG, "Checking if the field exists was successful");
                firebaseCallback.onCallback(fieldExists);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Checking if the field exists failed: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }
    public void updateTheField(String documentPath, String fieldName, Object fieldValue) {
        DocumentReference documentReference = db.document(documentPath);
        HashMap<String, Object> data = new HashMap<>();
        data.put(fieldName, fieldValue);
        documentReference.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully updated the field");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to update the field");
            }
        });;
    }

    public void deleteField(String documentPath, final String fieldName) {
        DocumentReference documentReference = db.document(documentPath);
        HashMap<String, Object> data = new HashMap<>();
        data.put(fieldName, FieldValue.delete());
        documentReference.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully deleted the field");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to delete the field");
                e.printStackTrace();

            }
        });
    }

    public void saveFileInFirebaseStorage(String savingPath, Uri uri, final FirebaseCallback firebaseCallback) {
        storageReference = FirebaseStorage.getInstance().getReference().child(savingPath);

        if (uri != null) {
            storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.d(TAG, "File successfully saved");
                    firebaseCallback.onCallback(null);
                }
            });
        }
    }

    public void getTheFileUriFromFirebaseStorage(String filePath, final FirebaseCallback firebaseCallback) {
        storageReference = FirebaseStorage.getInstance().getReference().child(filePath);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "Successfully got the file Uri: " + uri);
                firebaseCallback.onCallback(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to get the file Uri: " + e);
                firebaseCallback.onCallback(null);
            }
        });
    }

    public void deleteFileFromFirebaseStorage(String filePath) {
        storageReference = FirebaseStorage.getInstance().getReference().child(filePath);

        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully deleted the file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to delete the file: " + e);
            }
        });
    }

    public DocumentReference getDocumentReference(String documentPath) {
        DocumentReference documentReference = db.document(documentPath);
        return documentReference;
    }

}
