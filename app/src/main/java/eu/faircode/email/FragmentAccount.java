package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_NONE;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;

public class FragmentAccount extends FragmentBase {
    private ViewGroup view;
    private ScrollView scroll;

    private Spinner spProvider;

    private EditText etDomain;
    private Button btnAutoConfig;

    private EditText etHost;
    private RadioGroup rgEncryption;
    private CheckBox cbInsecure;
    private EditText etPort;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private EditText etRealm;

    private EditText etName;
    private Button btnColor;
    private View vwColor;
    private ImageButton ibColorDefault;

    private Button btnAdvanced;
    private CheckBox cbSynchronize;
    private CheckBox cbPrimary;
    private CheckBox cbNotify;
    private CheckBox cbBrowse;
    private EditText etInterval;
    private CheckBox cbPartialFetch;

    private Button btnCheck;
    private ContentLoadingProgressBar pbCheck;
    private TextView tvIdle;
    private TextView tvUtf8;

    private ArrayAdapter<EntityFolder> adapter;
    private Spinner spDrafts;
    private Spinner spSent;
    private Spinner spArchive;
    private Spinner spTrash;
    private Spinner spJunk;
    private Spinner spLeft;
    private Spinner spRight;

    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private TextView tvError;
    private TextView tvInstructions;

    private ContentLoadingProgressBar pbWait;

    private Group grpServer;
    private Group grpAuthorize;
    private Group grpAdvanced;
    private Group grpFolders;

    private long id = -1;
    private boolean saving = false;
    private int color = Color.TRANSPARENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = args.getLong("id", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_account);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);
        scroll = view.findViewById(R.id.scroll);

        // Get controls
        spProvider = view.findViewById(R.id.spProvider);

        etDomain = view.findViewById(R.id.etDomain);
        btnAutoConfig = view.findViewById(R.id.btnAutoConfig);

        etHost = view.findViewById(R.id.etHost);
        etPort = view.findViewById(R.id.etPort);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        cbInsecure = view.findViewById(R.id.cbInsecure);
        etUser = view.findViewById(R.id.etUser);
        tilPassword = view.findViewById(R.id.tilPassword);
        etRealm = view.findViewById(R.id.etRealm);

        etName = view.findViewById(R.id.etName);
        btnColor = view.findViewById(R.id.btnColor);
        vwColor = view.findViewById(R.id.vwColor);
        ibColorDefault = view.findViewById(R.id.ibColorDefault);

        btnAdvanced = view.findViewById(R.id.btnAdvanced);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        cbNotify = view.findViewById(R.id.cbNotify);
        cbBrowse = view.findViewById(R.id.cbBrowse);
        etInterval = view.findViewById(R.id.etInterval);
        cbPartialFetch = view.findViewById(R.id.cbPartialFetch);

        btnCheck = view.findViewById(R.id.btnCheck);
        pbCheck = view.findViewById(R.id.pbCheck);

        tvIdle = view.findViewById(R.id.tvIdle);
        tvUtf8 = view.findViewById(R.id.tvUtf8);

        spDrafts = view.findViewById(R.id.spDrafts);
        spSent = view.findViewById(R.id.spSent);
        spArchive = view.findViewById(R.id.spArchive);
        spTrash = view.findViewById(R.id.spTrash);
        spJunk = view.findViewById(R.id.spJunk);
        spLeft = view.findViewById(R.id.spLeft);
        spRight = view.findViewById(R.id.spRight);

        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        tvError = view.findViewById(R.id.tvError);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        pbWait = view.findViewById(R.id.pbWait);

        grpServer = view.findViewById(R.id.grpServer);
        grpAuthorize = view.findViewById(R.id.grpAuthorize);
        grpAdvanced = view.findViewById(R.id.grpAdvanced);
        grpFolders = view.findViewById(R.id.grpFolders);

        // Wire controls

        spProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemid) {
                EmailProvider provider = (EmailProvider) adapterView.getSelectedItem();
                grpServer.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                grpAuthorize.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

                btnAdvanced.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                if (position == 0)
                    grpAdvanced.setVisibility(View.GONE);

                btnCheck.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                tvIdle.setVisibility(View.GONE);
                tvUtf8.setVisibility(View.GONE);

                Object tag = adapterView.getTag();
                if (tag != null && (Integer) tag == position)
                    return;
                adapterView.setTag(position);

                etHost.setText(provider.imap_host);
                etPort.setText(provider.imap_host == null ? null : Integer.toString(provider.imap_port));
                rgEncryption.check(provider.imap_starttls ? R.id.radio_starttls : R.id.radio_ssl);

                etUser.setTag(null);
                etUser.setText(null);
                tilPassword.getEditText().setText(null);
                etRealm.setText(null);

                etName.setText(position > 1 ? provider.name : null);

                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        etDomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                btnAutoConfig.setEnabled(text.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnAutoConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAutoConfig();
            }
        });

        rgEncryption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                etPort.setHint(id == R.id.radio_starttls ? "143" : "993");
            }
        });

        setColor(color);
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] colors = getContext().getResources().getIntArray(R.array.colorPicker);
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(R.string.title_account_color, colors, color, 4, colors.length);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        if (!Helper.isPro(getContext())) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SHOW_PRO));
                            return;
                        }

                        setColor(color);
                    }
                });
                colorPickerDialog.show(getFragmentManager(), "colorpicker");
            }
        });

        ibColorDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(Color.TRANSPARENT);
            }
        });

        btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = (grpAdvanced.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                grpAdvanced.setVisibility(visibility);
                if (visibility == View.VISIBLE)
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.smoothScrollTo(0, btnAdvanced.getTop());
                        }
                    });
            }
        });

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbPrimary.setEnabled(checked);
            }
        });

        cbNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !Helper.isPro(getContext())) {
                    cbNotify.setChecked(false);

                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                    lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SHOW_PRO));
                }
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Helper.hide(cbNotify);
            Helper.hide(view.findViewById(R.id.tvNotifyPro));
        }

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheck();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(false);
            }
        });

        adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spDrafts.setAdapter(adapter);
        spSent.setAdapter(adapter);
        spArchive.setAdapter(adapter);
        spTrash.setAdapter(adapter);
        spJunk.setAdapter(adapter);
        spLeft.setAdapter(adapter);
        spRight.setAdapter(adapter);

        addBackPressedListener(new ActivityBase.IBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                onSave(true);
                return true;
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);

        btnAutoConfig.setEnabled(false);

        rgEncryption.setVisibility(View.GONE);
        cbInsecure.setVisibility(View.GONE);
        tilPassword.setEndIconMode(id < 0 ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);

        btnAdvanced.setVisibility(View.GONE);

        tvIdle.setVisibility(View.GONE);
        tvUtf8.setVisibility(View.GONE);

        btnCheck.setVisibility(View.GONE);
        pbCheck.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        pbSave.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvInstructions.setMovementMethod(LinkMovementMethod.getInstance());

        grpServer.setVisibility(View.GONE);
        grpAuthorize.setVisibility(View.GONE);
        grpAdvanced.setVisibility(View.GONE);
        grpFolders.setVisibility(View.GONE);

        return view;
    }

    private void onAutoConfig() {
        Bundle args = new Bundle();
        args.putString("domain", etDomain.getText().toString());

        new SimpleTask<EmailProvider>() {
            @Override
            protected void onPreExecute(Bundle args) {
                etDomain.setEnabled(false);
                btnAutoConfig.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                etDomain.setEnabled(true);
                btnAutoConfig.setEnabled(true);
            }

            @Override
            protected EmailProvider onExecute(Context context, Bundle args) throws Throwable {
                String domain = args.getString("domain");
                return EmailProvider.fromDomain(context, domain);
            }

            @Override
            protected void onExecuted(Bundle args, EmailProvider provider) {
                etHost.setText(provider.imap_host);
                etPort.setText(Integer.toString(provider.imap_port));
                rgEncryption.check(provider.imap_starttls ? R.id.radio_starttls : R.id.radio_ssl);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentAccount.this, args, "account:config");
    }

    private void onCheck() {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("realm", etRealm.getText().toString());

        new SimpleTask<CheckResult>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbCheck.setVisibility(View.VISIBLE);
                tvIdle.setVisibility(View.GONE);
                tvUtf8.setVisibility(View.GONE);
                grpFolders.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
                tvInstructions.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbCheck.setVisibility(View.GONE);
            }

            @Override
            protected CheckResult onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");
                String realm = args.getString("realm");

                if (TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (starttls ? "143" : "993");
                if (TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));

                if (TextUtils.isEmpty(realm))
                    realm = null;

                DB db = DB.getInstance(context);

                CheckResult result = new CheckResult();
                result.account = db.account().getAccount(id);
                result.folders = new ArrayList<>();

                // Check IMAP server / get folders
                Properties props = MessageHelper.getSessionProperties(realm, insecure);
                Session isession = Session.getInstance(props, null);
                isession.setDebug(true);
                try (Store istore = isession.getStore("imap" + (starttls ? "" : "s"))) {
                    istore.connect(host, Integer.parseInt(port), user, password);

                    result.idle = ((IMAPStore) istore).hasCapability("IDLE");

                    boolean inbox = false;
                    boolean archive = false;
                    boolean drafts = false;
                    boolean trash = false;
                    boolean sent = false;
                    boolean junk = false;
                    EntityFolder altArchive = null;
                    EntityFolder altDrafts = null;
                    EntityFolder altTrash = null;
                    EntityFolder altSent = null;
                    EntityFolder altJunk = null;

                    for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                        // Check folder attributes
                        String fullName = ifolder.getFullName();
                        String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                        Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs));
                        String type = EntityFolder.getType(attrs, fullName);

                        if (type != null) {
                            // Create entry
                            EntityFolder folder = db.folder().getFolderByName(id, fullName);
                            if (folder == null) {
                                int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
                                folder = new EntityFolder();
                                folder.name = fullName;
                                folder.type = type;
                                folder.synchronize = (sync >= 0);
                                folder.download = (sync < 0 ? true : EntityFolder.SYSTEM_FOLDER_DOWNLOAD.get(sync));
                                folder.sync_days = EntityFolder.DEFAULT_SYNC;
                                folder.keep_days = EntityFolder.DEFAULT_KEEP;
                            }
                            result.folders.add(folder);

                            if (EntityFolder.USER.equals(type)) {
                                if (folder.name.toLowerCase().contains("archive"))
                                    altArchive = folder;
                                if (folder.name.toLowerCase().contains("draft"))
                                    altDrafts = folder;
                                if (folder.name.toLowerCase().contains("trash"))
                                    altTrash = folder;
                                if (folder.name.toLowerCase().contains("sent"))
                                    altSent = folder;
                                if (folder.name.toLowerCase().contains("junk"))
                                    altJunk = folder;
                            } else {
                                if (EntityFolder.INBOX.equals(type))
                                    inbox = true;
                                else if (EntityFolder.ARCHIVE.equals(type))
                                    archive = true;
                                else if (EntityFolder.DRAFTS.equals(type))
                                    drafts = true;
                                else if (EntityFolder.TRASH.equals(type))
                                    trash = true;
                                else if (EntityFolder.SENT.equals(type))
                                    sent = true;
                                else if (EntityFolder.JUNK.equals(type))
                                    junk = true;
                            }

                            if (EntityFolder.INBOX.equals(type))
                                result.utf8 = (Boolean) ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                                    @Override
                                    public Object doCommand(IMAPProtocol protocol) {
                                        return protocol.supportsUtf8();
                                    }
                                });

                            Log.i(folder.name + " id=" + folder.id +
                                    " type=" + folder.type + " attr=" + TextUtils.join(",", attrs));
                        }
                    }

                    if (!inbox)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_inbox));
                    if (!archive && altArchive != null)
                        altArchive.type = EntityFolder.ARCHIVE;
                    if (!drafts && altDrafts != null)
                        altDrafts.type = EntityFolder.DRAFTS;
                    if (!trash && altTrash != null)
                        altTrash.type = EntityFolder.TRASH;
                    if (!sent && altSent != null)
                        altSent.type = EntityFolder.SENT;
                    if (!junk && altJunk != null)
                        altJunk.type = EntityFolder.JUNK;

                    if (result.folders.size() > 0)
                        Collections.sort(result.folders, result.folders.get(0).getComparator(context));
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, CheckResult result) {
                tvIdle.setVisibility(result.idle ? View.GONE : View.VISIBLE);
                tvUtf8.setVisibility(result.utf8 == null || result.utf8 ? View.GONE : View.VISIBLE);

                setFolders(result.folders, result.account);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.smoothScrollTo(0, btnSave.getBottom());
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                grpFolders.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);

                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    showError(ex);
            }
        }.execute(FragmentAccount.this, args, "account:check");
    }

    private void onSave(boolean should) {
        EntityFolder drafts = (EntityFolder) spDrafts.getSelectedItem();
        EntityFolder sent = (EntityFolder) spSent.getSelectedItem();
        EntityFolder archive = (EntityFolder) spArchive.getSelectedItem();
        EntityFolder trash = (EntityFolder) spTrash.getSelectedItem();
        EntityFolder junk = (EntityFolder) spJunk.getSelectedItem();
        EntityFolder left = (EntityFolder) spLeft.getSelectedItem();
        EntityFolder right = (EntityFolder) spRight.getSelectedItem();

        if (drafts != null && drafts.type == null)
            drafts = null;
        if (sent != null && sent.type == null)
            sent = null;
        if (archive != null && archive.type == null)
            archive = null;
        if (trash != null && trash.type == null)
            trash = null;
        if (junk != null && junk.type == null)
            junk = null;
        if (left != null && left.type == null)
            left = null;
        if (right != null && right.type == null)
            right = null;

        Bundle args = new Bundle();
        args.putLong("id", id);

        args.putString("host", etHost.getText().toString());
        args.putBoolean("starttls", rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls);
        args.putBoolean("insecure", cbInsecure.isChecked());
        args.putString("port", etPort.getText().toString());
        args.putString("user", etUser.getText().toString());
        args.putString("password", tilPassword.getEditText().getText().toString());
        args.putString("realm", etRealm.getText().toString());

        args.putString("name", etName.getText().toString());
        args.putInt("color", color);

        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("primary", cbPrimary.isChecked());
        args.putBoolean("notify", cbNotify.isChecked());
        args.putBoolean("browse", cbBrowse.isChecked());
        args.putString("interval", etInterval.getText().toString());
        args.putBoolean("partial_fetch", cbPartialFetch.isChecked());

        args.putSerializable("drafts", drafts);
        args.putSerializable("sent", sent);
        args.putSerializable("archive", archive);
        args.putSerializable("trash", trash);
        args.putSerializable("junk", junk);
        args.putSerializable("left", left);
        args.putSerializable("right", right);

        args.putBoolean("should", should);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                String host = args.getString("host");
                boolean starttls = args.getBoolean("starttls");
                boolean insecure = args.getBoolean("insecure");
                String port = args.getString("port");
                String user = args.getString("user");
                String password = args.getString("password");
                String realm = args.getString("realm");

                String name = args.getString("name");
                Integer color = args.getInt("color");

                boolean synchronize = args.getBoolean("synchronize");
                boolean primary = args.getBoolean("primary");
                boolean notify = args.getBoolean("notify");
                boolean browse = args.getBoolean("browse");
                String interval = args.getString("interval");
                boolean partial_fetch = args.getBoolean("partial_fetch");

                EntityFolder drafts = (EntityFolder) args.getSerializable("drafts");
                EntityFolder sent = (EntityFolder) args.getSerializable("sent");
                EntityFolder archive = (EntityFolder) args.getSerializable("archive");
                EntityFolder trash = (EntityFolder) args.getSerializable("trash");
                EntityFolder junk = (EntityFolder) args.getSerializable("junk");
                EntityFolder left = (EntityFolder) args.getSerializable("left");
                EntityFolder right = (EntityFolder) args.getSerializable("right");

                boolean should = args.getBoolean("should");

                if (!should && TextUtils.isEmpty(host))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_host));
                if (TextUtils.isEmpty(port))
                    port = (starttls ? "143" : "993");
                if (!should && TextUtils.isEmpty(user))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_user));
                if (!should && synchronize && TextUtils.isEmpty(password) && !insecure)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_password));
                if (TextUtils.isEmpty(interval))
                    interval = Integer.toString(EntityAccount.DEFAULT_KEEP_ALIVE_INTERVAL);

                if (TextUtils.isEmpty(realm))
                    realm = null;
                if (TextUtils.isEmpty(name))
                    name = user;
                if (Color.TRANSPARENT == color)
                    color = null;

                long now = new Date().getTime();

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(id);

                if (should) {
                    if (account == null)
                        return !TextUtils.isEmpty(host) && !TextUtils.isEmpty(user);

                    if (!Objects.equals(account.host, host))
                        return true;
                    if (!Objects.equals(account.starttls, starttls))
                        return true;
                    if (!Objects.equals(account.insecure, insecure))
                        return true;
                    if (!Objects.equals(account.port, Integer.parseInt(port)))
                        return true;
                    if (!Objects.equals(account.user, user))
                        return true;
                    if (!Objects.equals(account.password, password))
                        return true;
                    if (!Objects.equals(account.realm, realm))
                        return true;
                    if (!Objects.equals(account.name, name))
                        return true;
                    if (!Objects.equals(account.color, color))
                        return true;
                    if (!Objects.equals(account.synchronize, synchronize))
                        return true;
                    if (!Objects.equals(account.primary, account.synchronize && primary))
                        return true;
                    if (!Objects.equals(account.notify, notify))
                        return true;
                    if (!Objects.equals(account.browse, browse))
                        return true;
                    if (!Objects.equals(account.poll_interval, Integer.parseInt(interval)))
                        return true;
                    if (!Objects.equals(account.partial_fetch, partial_fetch))
                        return true;

                    EntityFolder edrafts = db.folder().getFolderByType(account.id, EntityFolder.DRAFTS);
                    if (!Objects.equals(edrafts == null ? null : edrafts.id, drafts == null ? null : drafts.id))
                        return true;

                    EntityFolder esent = db.folder().getFolderByType(account.id, EntityFolder.SENT);
                    if (!Objects.equals(esent == null ? null : esent.id, sent == null ? null : sent.id))
                        return true;

                    EntityFolder earchive = db.folder().getFolderByType(account.id, EntityFolder.ARCHIVE);
                    if (!Objects.equals(earchive == null ? null : earchive.id, archive == null ? null : archive.id))
                        return true;

                    EntityFolder etrash = db.folder().getFolderByType(account.id, EntityFolder.TRASH);
                    if (!Objects.equals(etrash == null ? null : etrash.id, trash == null ? null : trash.id))
                        return true;

                    EntityFolder ejunk = db.folder().getFolderByType(account.id, EntityFolder.JUNK);
                    if (!Objects.equals(ejunk == null ? null : ejunk.id, junk == null ? null : junk.id))
                        return true;

                    if (!Objects.equals(account.swipe_left, left == null ? null : left.id == null ? -1L : left.id))
                        return true;
                    if (!Objects.equals(account.swipe_right, right == null ? null : right.id == null ? -1L : right.id))
                        return true;

                    if (account.error != null)
                        return true;

                    return false;
                }

                String accountRealm = (account == null ? null : account.realm);

                boolean check = (synchronize && (account == null ||
                        !account.synchronize || account.error != null ||
                        !host.equals(account.host) || Integer.parseInt(port) != account.port ||
                        !user.equals(account.user) || !password.equals(account.password) ||
                        !Objects.equals(realm, accountRealm) ||
                        !TextUtils.isEmpty(account.error)));
                boolean reload = (check || account == null ||
                        account.synchronize != synchronize ||
                        account.notify != notify ||
                        !account.poll_interval.equals(Integer.parseInt(interval)) ||
                        account.partial_fetch != partial_fetch);

                Long last_connected = null;
                if (account != null && synchronize == account.synchronize)
                    last_connected = account.last_connected;

                // Check IMAP server
                EntityFolder inbox = null;
                if (check) {
                    Properties props = MessageHelper.getSessionProperties(realm, insecure);
                    Session isession = Session.getInstance(props, null);
                    isession.setDebug(true);

                    try (Store istore = isession.getStore("imap" + (starttls ? "" : "s"))) {
                        istore.connect(host, Integer.parseInt(port), user, password);

                        for (Folder ifolder : istore.getDefaultFolder().list("*")) {
                            // Check folder attributes
                            String fullName = ifolder.getFullName();
                            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs));
                            String type = EntityFolder.getType(attrs, fullName);

                            if (EntityFolder.INBOX.equals(type)) {
                                inbox = new EntityFolder();
                                inbox.name = fullName;
                                inbox.type = type;
                                inbox.synchronize = true;
                                inbox.unified = true;
                                inbox.notify = true;
                                inbox.sync_days = EntityFolder.DEFAULT_SYNC;
                                inbox.keep_days = EntityFolder.DEFAULT_KEEP;
                            }
                        }
                    }
                }

                try {
                    db.beginTransaction();

                    boolean update = (account != null);
                    if (account == null)
                        account = new EntityAccount();

                    account.auth_type = ConnectionHelper.AUTH_TYPE_PASSWORD;
                    account.host = host;
                    account.starttls = starttls;
                    account.insecure = insecure;
                    account.port = Integer.parseInt(port);
                    account.user = user;
                    account.password = password;
                    account.realm = realm;

                    account.name = name;
                    account.color = color;

                    account.synchronize = synchronize;
                    account.primary = (account.synchronize && primary);
                    account.notify = notify;
                    account.browse = browse;
                    account.poll_interval = Integer.parseInt(interval);
                    account.partial_fetch = partial_fetch;

                    if (!update)
                        account.created = now;

                    account.warning = null;
                    account.error = null;
                    account.last_connected = last_connected;

                    if (account.primary)
                        db.account().resetPrimary();

                    if (!Helper.isPro(context)) {
                        account.color = null;
                        account.notify = false;
                    }

                    if (update)
                        db.account().updateAccount(account);
                    else
                        account.id = db.account().insertAccount(account);
                    EntityLog.log(context, (update ? "Updated" : "Added") + " account=" + account.name);

                    // Make sure the channel exists on commit
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (account.notify) {
                            // Add or update notification channel
                            account.deleteNotificationChannel(context);
                            account.createNotificationChannel(context);
                        } else if (!account.synchronize)
                            account.deleteNotificationChannel(context);
                    }

                    List<EntityFolder> folders = new ArrayList<>();

                    if (inbox != null)
                        folders.add(inbox);

                    if (drafts != null) {
                        drafts.type = EntityFolder.DRAFTS;
                        folders.add(drafts);
                    }

                    if (sent != null) {
                        sent.type = EntityFolder.SENT;
                        folders.add(sent);
                    }
                    if (archive != null) {
                        archive.type = EntityFolder.ARCHIVE;
                        folders.add(archive);
                    }
                    if (trash != null) {
                        trash.type = EntityFolder.TRASH;
                        folders.add(trash);
                    }
                    if (junk != null) {
                        junk.type = EntityFolder.JUNK;
                        folders.add(junk);
                    }

                    if (left != null) {
                        boolean found = false;
                        for (EntityFolder folder : folders)
                            if (left.name.equals(folder.name)) {
                                found = true;
                                break;
                            }
                        if (!found) {
                            left.type = EntityFolder.USER;
                            folders.add(left);
                        }
                    }

                    if (right != null) {
                        boolean found = false;
                        for (EntityFolder folder : folders)
                            if (right.name.equals(folder.name)) {
                                found = true;
                                break;
                            }
                        if (!found) {
                            right.type = EntityFolder.USER;
                            folders.add(right);
                        }
                    }

                    db.folder().setFoldersUser(account.id);

                    for (EntityFolder folder : folders) {
                        EntityFolder existing = db.folder().getFolderByName(account.id, folder.name);
                        if (existing == null) {
                            folder.account = account.id;
                            EntityLog.log(context, "Added folder=" + folder.name + " type=" + folder.type);
                            folder.id = db.folder().insertFolder(folder);
                        } else {
                            EntityLog.log(context, "Updated folder=" + folder.name + " type=" + folder.type);
                            db.folder().setFolderType(existing.id, folder.type);
                        }
                    }

                    account.swipe_left = (left == null ? null : left.id);
                    account.swipe_right = (right == null ? null : right.id);
                    db.account().updateAccount(account);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload)
                    ServiceSynchronize.reload(context, "save account");

                if (!synchronize) {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("receive", account.id.intValue());
                }

                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean dirty) {
                if (dirty)
                    new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                            .setMessage(R.string.title_ask_save)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            scroll.smoothScrollTo(0, btnSave.getBottom());
                                        }
                                    });
                                    onSave(false);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getFragmentManager().popBackStack();
                                }
                            })
                            .show();
                else
                    getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    showError(ex);
            }
        }.execute(FragmentAccount.this, args, "account:save");
    }

    private void showError(Throwable ex) {
        tvError.setText(Helper.formatThrowable(ex));
        tvError.setVisibility(View.VISIBLE);

        final View target;

        EmailProvider provider = (EmailProvider) spProvider.getSelectedItem();
        if (provider != null && provider.documentation != null) {
            tvInstructions.setText(HtmlHelper.fromHtml(provider.documentation.toString()));
            tvInstructions.setVisibility(View.VISIBLE);
            target = tvInstructions;
        } else
            target = tvError;

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scroll.smoothScrollTo(0, target.getBottom());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("fair:provider", spProvider.getSelectedItemPosition());
        outState.putString("fair:password", tilPassword.getEditText().getText().toString());
        outState.putInt("fair:advanced", grpAdvanced.getVisibility());
        outState.putInt("fair:color", color);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityAccount>() {
            @Override
            protected EntityAccount onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).account().getAccount(id);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityAccount account) {
                // Get providers
                List<EmailProvider> providers = EmailProvider.loadProfiles(getContext());
                providers.add(0, new EmailProvider(getString(R.string.title_select)));
                providers.add(1, new EmailProvider(getString(R.string.title_custom)));

                ArrayAdapter<EmailProvider> aaProvider =
                        new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, providers);
                aaProvider.setDropDownViewResource(R.layout.spinner_item1_dropdown);
                spProvider.setAdapter(aaProvider);

                if (savedInstanceState == null) {
                    if (account != null) {
                        boolean found = false;
                        for (int pos = 2; pos < providers.size(); pos++) {
                            EmailProvider provider = providers.get(pos);
                            if (provider.imap_host.equals(account.host) &&
                                    provider.imap_port == account.port) {
                                found = true;
                                spProvider.setTag(pos);
                                spProvider.setSelection(pos);
                                break;
                            }
                        }
                        if (!found) {
                            spProvider.setTag(1);
                            spProvider.setSelection(1);
                        }
                        etHost.setText(account.host);
                        etPort.setText(Long.toString(account.port));
                    }

                    rgEncryption.check(account != null && account.starttls ? R.id.radio_starttls : R.id.radio_ssl);
                    cbInsecure.setChecked(account == null ? false : account.insecure);

                    etUser.setText(account == null ? null : account.user);
                    tilPassword.getEditText().setText(account == null ? null : account.password);
                    etRealm.setText(account == null ? null : account.realm);

                    etName.setText(account == null ? null : account.name);
                    cbNotify.setChecked(account != null && account.notify && Helper.isPro(getContext()));

                    cbSynchronize.setChecked(account == null ? true : account.synchronize);
                    cbPrimary.setChecked(account == null ? false : account.primary);
                    cbBrowse.setChecked(account == null ? true : account.browse);
                    etInterval.setText(account == null ? "" : Long.toString(account.poll_interval));
                    cbPartialFetch.setChecked(account == null ? true : account.partial_fetch);

                    color = (account == null || account.color == null ? Color.TRANSPARENT : account.color);

                    new SimpleTask<EntityAccount>() {
                        @Override
                        protected EntityAccount onExecute(Context context, Bundle args) {
                            return DB.getInstance(context).account().getPrimaryAccount();
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityAccount primary) {
                            if (primary == null)
                                cbPrimary.setChecked(true);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentAccount.this, new Bundle(), "account:primary");
                } else {
                    int provider = savedInstanceState.getInt("fair:provider");
                    spProvider.setTag(provider);
                    spProvider.setSelection(provider);

                    tilPassword.getEditText().setText(savedInstanceState.getString("fair:password"));
                    grpAdvanced.setVisibility(savedInstanceState.getInt("fair:advanced"));
                    color = savedInstanceState.getInt("fair:color");
                }

                Helper.setViewsEnabled(view, true);

                setColor(color);
                cbPrimary.setEnabled(cbSynchronize.isChecked());

                // Consider previous check/save/delete as cancelled
                pbWait.setVisibility(View.GONE);

                args.putLong("account", account == null ? -1 : account.id);

                new SimpleTask<List<EntityFolder>>() {
                    @Override
                    protected List<EntityFolder> onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");

                        DB db = DB.getInstance(context);
                        List<EntityFolder> folders = db.folder().getFolders(account);

                        if (folders != null && folders.size() > 0)
                            Collections.sort(folders, folders.get(0).getComparator(context));

                        return folders;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityFolder> folders) {
                        if (folders == null)
                            folders = new ArrayList<>();
                        setFolders(folders, account);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentAccount.this, args, "account:folders");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "account:get");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete).setVisible(id > 0 && !saving);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                onMenuDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDelete() {
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_account_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", id);

                        new SimpleTask<Void>() {
                            @Override
                            protected void onPostExecute(Bundle args) {
                                Helper.setViewsEnabled(view, false);
                                pbWait.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");

                                DB db = DB.getInstance(context);
                                db.account().setAccountTbd(id);

                                ServiceSynchronize.reload(context, "delete account");

                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                getFragmentManager().popBackStack();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentAccount.this, args, "account:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setColor(int color) {
        this.color = color;

        GradientDrawable border = new GradientDrawable();
        border.setColor(color);
        border.setStroke(1, Helper.resolveColor(getContext(), R.attr.colorSeparator));
        vwColor.setBackground(border);
    }

    private void setFolders(List<EntityFolder> _folders, EntityAccount account) {
        List<EntityFolder> folders = new ArrayList<>();
        for (EntityFolder folder : _folders)
            if (!EntityFolder.INBOX.equals(folder.type))
                folders.add(folder);

        EntityFolder none = new EntityFolder();
        none.id = -1L;
        none.name = "-";
        folders.add(0, none);

        adapter.clear();
        adapter.addAll(folders);

        Long left = (account == null ? null : account.swipe_left);
        Long right = (account == null ? null : account.swipe_right);

        String leftDefault = EntityFolder.TRASH;
        String rightDefault = EntityFolder.TRASH;
        for (EntityFolder folder : folders)
            if (EntityFolder.ARCHIVE.equals(folder.type)) {
                rightDefault = folder.type;
                break;
            }

        for (int pos = 0; pos < folders.size(); pos++) {
            EntityFolder folder = folders.get(pos);

            if (EntityFolder.DRAFTS.equals(folder.type))
                spDrafts.setSelection(pos);
            else if (EntityFolder.SENT.equals(folder.type))
                spSent.setSelection(pos);
            else if (EntityFolder.ARCHIVE.equals(folder.type))
                spArchive.setSelection(pos);
            else if (EntityFolder.TRASH.equals(folder.type))
                spTrash.setSelection(pos);
            else if (EntityFolder.JUNK.equals(folder.type))
                spJunk.setSelection(pos);

            if (left == null ? (account == null && leftDefault.equals(folder.type)) : left.equals(folder.id))
                spLeft.setSelection(pos);

            if (right == null ? (account == null && rightDefault.equals(folder.type)) : right.equals(folder.id))
                spRight.setSelection(pos);
        }

        grpFolders.setVisibility(folders.size() > 1 ? View.VISIBLE : View.GONE);
        btnSave.setVisibility(folders.size() > 1 ? View.VISIBLE : View.GONE);
    }

    private class CheckResult {
        EntityAccount account;
        List<EntityFolder> folders;
        boolean idle;
        Boolean utf8;
    }
}
