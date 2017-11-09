package esolutions.com.esdatabaselib.example.activity;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import esolutions.com.esdatabaselib.R;
import esolutions.com.esdatabaselib.baseSqlite.LazyList;
import esolutions.com.esdatabaselib.baseSqlite.SqlDAO;
import esolutions.com.esdatabaselib.baseSqlite.SqlHelper;
import esolutions.com.esdatabaselib.example.source.sqliteConfig.ClassRoom;
import esolutions.com.esdatabaselib.example.source.sqliteConfig.ESDbConfig;
import esolutions.com.esdatabaselib.example.source.sqliteConfig.Student;

import static android.util.Log.d;

public class MainActivity extends AppCompatActivity {

    private static final String PATH_LOG = Environment.getExternalStorageDirectory() + File.separator + "ES_DB_TEST" + File.separator;
    public static final String NAME_FILE_LOG = "ES_Database_Test.s3db";

    Button btnCreateTable, btnInsertData10k, btnReloadData;
    TextView tvPath;
    ListView lvData;

    Thread threadCreateDB, theadInsertData10k, threadReloadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        (btnCreateTable = (Button) findViewById(R.id.btnCreateTable)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (threadCreateDB != null && threadCreateDB.isAlive()) {
                    Toast.makeText(MainActivity.this, "Đang trong quá trình tạo db, thử lại sau!", Toast.LENGTH_SHORT).show();
                    return;
                }

                threadCreateDB = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SqlHelper.setupDB(MainActivity.this, ESDbConfig.class, new Class[]{
                                    ClassRoom.class, Student.class});
                            //try reload because lib reload is not working
                            MainActivity.this.createFileIfNotExist(PATH_LOG + NAME_FILE_LOG);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        (tvPath = (TextView) findViewById(R.id.tvPathFileDB)).setText(SqlHelper.getDatabasePath());
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            e.printStackTrace();
                        }
                    }
                });
                threadCreateDB.start();
            }
        });

        (btnInsertData10k = (Button) findViewById(R.id.btnInsertData10k)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (theadInsertData10k != null && theadInsertData10k.isAlive()) {
                    Toast.makeText(MainActivity.this, "Đang trong quá trình insert dữ liệu, thử lại sau!", Toast.LENGTH_SHORT).show();
                    return;
                }

                theadInsertData10k = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SqlDAO sqlDAO = new SqlDAO(SqlHelper.getIntance().openDB(), MainActivity.this);
                            sqlDAO.deleteAll(Student.class);

                            //create list 10k student
                            List<Student> listDataDump = new ArrayList<Student>();
                            for (int i = 0; i < 10000; i++) {
                                Student aStudent = new Student();
                                aStudent.setmName("Học sinh thứ " + i + 1);
                                String className = (i + 1) % 10000 + "";
                                aStudent.setClassName("Lớp " + className);
                                listDataDump.add(aStudent);
                            }

                            Long[] indexs = sqlDAO.insert(listDataDump);

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        fillListView();
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                });
                theadInsertData10k.start();
            }
        });

        (btnReloadData=(Button)findViewById(R.id.btnReloadData)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (threadReloadData != null && threadReloadData.isAlive()) {
                    Toast.makeText(MainActivity.this, "Đang trong quá trình reload dữ liệu hiển thị, thử lại sau!", Toast.LENGTH_SHORT).show();
                    return;
                }

                threadReloadData = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    fillListView();
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

                threadReloadData.start();
            }
        });
    }

    private void fillListView() throws Exception {
        SqlDAO sqlDAO = new SqlDAO(SqlHelper.getIntance().openDB(), MainActivity.this);
        LazyList<Student> students = sqlDAO.selectAllLazy(Student.class, null);

        List<String> listData = new ArrayList<>();
        for(int i = 0; i<students.size() ;i++)
        {
//        for (Student student : students) {
//            listData.add(student != null ? student.getmName() + student.getClassName() : null);

            listData.add(students.get(i).getmName() + " " + students.get(i).getClassName());
        }

        (lvData = (ListView) findViewById(R.id.lvdata)).
                setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, listData));

    }

    private boolean createFileIfNotExist(String sUriLogFile) throws Exception {
        //create file
        File file = new File(sUriLogFile);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            showFolderSdcard(sUriLogFile);
            return false;
        }

        return true;
    }

    private void showFolderSdcard(String sUriLogFile) throws Exception {
        // Load root folder
        File forder = new File(sUriLogFile);
        String parentPath = forder.getParentFile().getName();
        File folderParent = new File(parentPath);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String[] allFilesRoot = forder.list();
            for (int i = 0; i < allFilesRoot.length; i++) {
                allFilesRoot[i] = folderParent + allFilesRoot[i];
            }
            if (allFilesRoot != null)
                MediaScannerConnection.scanFile(this, allFilesRoot, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                d("ExternalStorage", "Scanned " + path + ":");
                                d("ExternalStorage", "uri=" + uri);
                            }
                        });
        } else {
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
                    .parse("file://" + parentPath)));
        }
    }
}
