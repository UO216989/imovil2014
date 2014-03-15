package es.uniovi.imovil.fcrtrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.json.JSONException;

import es.uniovi.imovil.fcrtrainer.highscores.Highscore;
import es.uniovi.imovil.fcrtrainer.highscores.HighscoreManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class HighscoresFragment extends Fragment implements OnItemSelectedListener {

	private static final String TAG = "HighscoresFragment";

	private final static int ALL_EXERCISES = -1; // negativo para que no coincida con un
													// id

	private View mRootView;
	private Spinner mExerciseSpinner;
	private ListView mHighscoreListView;
	ArrayList<Exercise> mExercises;

	public static HighscoresFragment newInstance() {
		HighscoresFragment fragment = new HighscoresFragment();
		return fragment;
	}

	public HighscoresFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.fragment_highscores, container, false);

		initializeExerciseSpinner();

		// El ListView se inicializa porque cuando se carga el spinner, se genera un
		// evento onItemSelected del spinner

		return mRootView;
	}

	private void initializeExerciseSpinner() {
		// La idea de esta funci�n es crear los elementos del spinner utilizando
		// los arrays definidos en los recursos

		mExercises = new ArrayList<Exercise>();
		Exercise all = new Exercise(getString(R.string.all), ALL_EXERCISES);
		mExercises.add(all);
		addExerciseModule(mExercises, R.array.codes);
		addExerciseModule(mExercises, R.array.digital_systems);
		addExerciseModule(mExercises, R.array.networks);

		ArrayAdapter<Exercise> adapter = new ArrayAdapter<Exercise>(getActivity(),
				android.R.layout.simple_list_item_1, mExercises);

		mExerciseSpinner = (Spinner) mRootView.findViewById(R.id.spinner_exercise);
		mExerciseSpinner.setAdapter(adapter);

		mExerciseSpinner.setOnItemSelectedListener(this);
	}

	private void addExerciseModule(ArrayList<Exercise> exercises, int arrayResourceId) {
		TypedArray array = getResources().obtainTypedArray(arrayResourceId);

		for (int i = 0; i < array.length(); i++) {
			int defaultId = 0;
			int resourceId = array.getResourceId(i, defaultId);

			Exercise exercise = new Exercise(getResources().getString(resourceId),
					resourceId);
			exercises.add(exercise);
		}

		array.recycle();
	}

	private void initializeListView(int selectedExerciseId) {
		mHighscoreListView = (ListView) mRootView.findViewById(R.id.list_view_highscores);
		ArrayList<Highscore> highscores = loadHighscores();

		if (highscores.size() == 0) {
			// Esta es la primera vez que se abre esta ventana, as� que a�adimos algunas
			// puntuaciones de ejemplo para que el usuario tenga algo que intentar batir
			addBasicHighscores();
			highscores = loadHighscores();
		}

		highscores = selectHighscores(highscores, selectedExerciseId);

		Collections.sort(highscores);
		Collections.reverse(highscores);

		HighscoreAdapter adapter = new HighscoreAdapter(getActivity(), highscores);
		mHighscoreListView.setAdapter(adapter);
	}

	private ArrayList<Highscore> loadHighscores() {
		ArrayList<Highscore> highscores = new ArrayList<Highscore>();

		try {
			highscores = HighscoreManager.loadHighscores(getActivity());
		} catch (JSONException e) {
			Log.d(TAG, "Error al analizar el JSON: " + e.getMessage());
			Toast.makeText(getActivity(),
					getActivity().getString(R.string.error_parsing_highscores),
					Toast.LENGTH_LONG).show();

			// Las puntuaciones estar�n vac�as
		}
		return highscores;
	}
	
	private ArrayList<Highscore> selectHighscores(ArrayList<Highscore> highscores, int selectedExerciseId) {
		ArrayList<Highscore> selectedHighscores = new ArrayList<Highscore>();

		for (Highscore highscore : highscores) {
			if (selectedExerciseId == ALL_EXERCISES
					|| selectedExerciseId == highscore.getExercise()) {
				selectedHighscores.add(highscore);
			}
		}
		return selectedHighscores;
	}

	private void addBasicHighscores() {
		String[] names = { "�ngel Manuel", "Beltr�n", "David Alejandro", "Diego", "Enol",
				"Gabriel", "Henrik", "In�s", "Luc�a", "Marcos", "Miguel �ngel", "�scar",
				"Raphael", "Roberto", "Walter" };
		for (Exercise exercise : mExercises) {
			if (exercise.getId() != ALL_EXERCISES) { // ALL_EXERCISES is not really an exercise
				addBasicScoresForExercise(names, exercise.getId());
			}
		}
	}

	private void addBasicScoresForExercise(String[] names, int exerciseId) {
		try {
			for (int i = 0; i < names.length; i++) {
				int minScore = 10;
				int maxScore = 100;
				int score = minScore
						+ (int) (Math.random() * ((maxScore - minScore) + 1));
				HighscoreManager.addScore(getActivity(), score, exerciseId,
						new Date(), names[i]);
			}
		} catch (JSONException e) {
			Log.d(TAG, "Error al analizar el JSON: " + e.getMessage());
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Exercise exercise = (Exercise) parent.getItemAtPosition(pos);
		initializeListView(exercise.getId());
	}

	@Override
	public void onNothingSelected(AdapterView<?> view) {
		// No hay que hacer nada
	}

}
