package ankur.dynamicview;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView formName;
    Button getForm, submit;
    LinearLayout form, fieldLayout;
    String formId = "", component = "", description = "", label = "", autofill = "";
    boolean editable = true, isReady = true;
    JSONArray fields;
    JSONObject field, body;
    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        formName = (TextView) findViewById(R.id.formName);
        getForm = (Button) findViewById(R.id.getForm);
        submit = (Button) findViewById(R.id.submit);
        form = (LinearLayout) findViewById(R.id.form);

        getForm.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getForm:

                form.removeAllViews();
                submit.setVisibility(View.GONE);

                if (isInternetOn()) {
                    getForm();
                } else {
                    Toast.makeText(this, "Please Check your Internet Connection.", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.submit:

                getFormData();

                break;
        }
    }


    public void getForm() {
        loading = ProgressDialog.show(this, "", "Please wait...", false, false);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://randomform.herokuapp.com", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.dismiss();
                Log.e("Response", response);

                createForm(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.e("VolleyError", error.toString());
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public void createForm(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            boolean success = Boolean.parseBoolean(jsonObject.getString("success"));

            if (success) {

                submit.setVisibility(View.VISIBLE);

                JSONObject data = jsonObject.getJSONObject("data");

                formId = data.getString("form_id");
                formName.setText(data.getString("form_name"));

                fields = data.getJSONArray("form_fields");

                for (int i = 0; i < fields.length(); i++) {
                    field = fields.getJSONObject(i);

                    component = field.getString("component");
                    description = field.getString("description");
                    label = field.getString("label");
                    editable = Boolean.parseBoolean(field.getString("editable"));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 100, 0, 0);

                    fieldLayout = new LinearLayout(this);
                    fieldLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView labelView = new TextView(this);
                    labelView.setText(label);
                    labelView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
                    labelView.setTextColor(getResources().getColor(R.color.colorBlack));
                    fieldLayout.addView(labelView);

                    switch (component) {
                        case "textarea":
                            if (!editable) {
                                autofill = field.getString("autofill");
                            } else {
                                autofill = "";
                            }
                            TextView();

                            break;

                        case "textinput":
                            if (!editable) {
                                autofill = field.getString("autofill");
                            } else {
                                autofill = "";
                            }
                            TextView();

                            break;

                        case "checkbox":
                            CheckBox();

                            break;

                        case "radio":
                            RadioButton();

                            break;

                        case "select":
                            Spinner();

                            break;
                    }

                    TextView descriptionView = new TextView(this);
                    descriptionView.setText("Description: " + description);
                    descriptionView.setTextAppearance(this, android.R.style.TextAppearance_Small);
                    descriptionView.setTextColor(getResources().getColor(R.color.colorPrimary));
                    fieldLayout.addView(descriptionView);

                    form.addView(fieldLayout, params);

                    Log.e("component", component);
                }
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }


    public void TextView() {
        EditText editText = new EditText(this);
        editText.setText(autofill);
        editText.setTextAppearance(this, android.R.style.TextAppearance_Small);
        editText.setEnabled(editable);
        editText.setTextColor(getResources().getColor(R.color.colorBlack));

        if (component.equalsIgnoreCase("textinput")) {
            editText.setSingleLine(true);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }

        fieldLayout.addView(editText);
    }

    public void CheckBox() {
        try {
            JSONArray options = field.getJSONArray("options");
            for (int i = 0; i < options.length(); i++) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(options.getString(i));

                if (!editable) {
                    JSONArray autoSelect = field.getJSONArray("autoselect");
                    for (int j = 0; j < autoSelect.length(); j++) {
                        if (autoSelect.getString(j).equalsIgnoreCase(options.getString(i))) {
                            checkBox.setChecked(true);
                        }
                    }
                }

                checkBox.setTextAppearance(this, android.R.style.TextAppearance_Small);
                checkBox.setEnabled(editable);
                checkBox.setTextColor(getResources().getColor(R.color.colorBlack));

                fieldLayout.addView(checkBox);
            }

        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    public void RadioButton() {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);

        try {
            JSONArray options = field.getJSONArray("options");
            for (int i = 0; i < options.length(); i++) {
                RadioButton radio = new RadioButton(this);
                radio.setText(options.getString(i));

                if (!editable) {
                    JSONArray autoSelect = field.getJSONArray("autoselect");
                    for (int j = 0; j < autoSelect.length(); j++) {
                        if (autoSelect.getString(j).equalsIgnoreCase(options.getString(i))) {
                            radio.setChecked(true);
                        }
                    }
                }

                radio.setTextAppearance(this, android.R.style.TextAppearance_Small);
                radio.setEnabled(editable);
                radio.setTextColor(getResources().getColor(R.color.colorBlack));

                group.addView(radio);
            }

        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }

        fieldLayout.addView(group);
    }

    public void Spinner() {
        try {
            JSONArray options = field.getJSONArray("options");
            ArrayList<String> optionsList = new ArrayList<>();
            optionsList.add("Select");
            for (int i = 0; i < options.length(); i++) {
                optionsList.add(options.getString(i));
            }

            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, optionsList);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);

            for (int i = 0; i < optionsList.size(); i++) {
                if (!editable) {
                    JSONArray autoSelect = field.getJSONArray("autoselect");
                    for (int j = 0; j < autoSelect.length(); j++) {
                        if (autoSelect.getString(j).equalsIgnoreCase(optionsList.get(i))) {
                            spinner.setSelection(i);
                            Log.e("spinner", optionsList.get(i));
                        }
                    }
                }
            }
            spinner.setEnabled(editable);

            fieldLayout.addView(spinner);

        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    public void getFormData() {
        isReady = true;
        try {
            body = new JSONObject();
            body.put("form_id", formId);

            Log.e("size", fields.length() + "");

            for (int i = 0; i < fields.length(); i++) {
                field = fields.getJSONObject(i);

                LinearLayout fieldLayout = (LinearLayout) form.getChildAt(i);

                component = field.getString("component");

                if (component.equalsIgnoreCase("textarea") || component.equalsIgnoreCase("textinput")) {
                    EditText editText = (EditText) fieldLayout.getChildAt(1);
                    body.put(field.getString("label"), editText.getText().toString().trim());
                    if (Boolean.parseBoolean(field.getString("required")) && editText.getText().toString().trim().equalsIgnoreCase("")) {
                        editText.setError("This field is required.");
                        isReady = false;
                        break;
                    }
                } else if (component.equalsIgnoreCase("checkbox")) {
                    JSONArray array = new JSONArray();
                    for (int j = 1; j < fieldLayout.getChildCount() - 1; j++) {
                        CheckBox checkBox = (CheckBox) fieldLayout.getChildAt(j);
                        if (checkBox.isChecked()) {
                            array.put(checkBox.getText().toString());
                        }
                    }

                    body.put(field.getString("label"), array);

                    if (Boolean.parseBoolean(field.getString("required")) && array.length() == 0) {
                        Toast.makeText(this, "Please select item for " + field.getString("label"), Toast.LENGTH_LONG).show();
                        isReady = false;
                        break;
                    }

                } else if (component.equalsIgnoreCase("radio")) {
                    RadioGroup group = (RadioGroup) fieldLayout.getChildAt(1);

                    String value = "";
                    for (int j = 0; j < group.getChildCount(); j++) {
                        RadioButton radioButton = (RadioButton) group.getChildAt(j);
                        if (radioButton.isChecked()) {
                            value = radioButton.getText().toString();
                        }
                    }

                    body.put(field.getString("label"), value);

                    if (Boolean.parseBoolean(field.getString("required")) && value.equalsIgnoreCase("")) {
                        Toast.makeText(this, "Please select item for " + field.getString("label"), Toast.LENGTH_LONG).show();
                        isReady = false;
                        break;
                    }
                } else if (component.equalsIgnoreCase("select")) {
                    Spinner spinner = (Spinner) fieldLayout.getChildAt(1);
                    String value = spinner.getSelectedItem().toString().trim();
                    if (value.equalsIgnoreCase("Select")) {
                        value = "";
                    }
                    body.put(field.getString("label"), value);
                    if (Boolean.parseBoolean(field.getString("required")) && value.equalsIgnoreCase("")) {
                        Toast.makeText(this, "Please select item for " + field.getString("label"), Toast.LENGTH_LONG).show();
                        isReady = false;
                        break;
                    }
                }
            }

            Log.e("object", body.toString());

            if (isReady) {
                if (isInternetOn()) {
                    submitForm();
                } else {
                    Toast.makeText(this, "Please Check your Internet Connection.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    public void submitForm() {
        loading = ProgressDialog.show(this, "", "Please wait...", false, false);

        JsonObjectRequest req = new JsonObjectRequest("https://randomform.herokuapp.com/submit", body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response.toString());
                loading.dismiss();
                try {
                    boolean success = Boolean.parseBoolean(response.getString("success"));

                    if (success) {
                        Toast.makeText(getApplicationContext(), "Form submitted Successfully!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", e.toString());
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                Log.e("VolleyError", error.toString());
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    public boolean isInternetOn() {

        boolean flag = false;
        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

            flag = true;

        } else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {

            flag = false;
        }
        return flag;
    }
}
