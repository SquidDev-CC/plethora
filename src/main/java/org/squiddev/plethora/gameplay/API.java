package org.squiddev.plethora.gameplay;

import org.squiddev.plethora.api.PlethoraGameplayAPI;
import org.squiddev.plethora.api.neural.INeuralRegistry;
import org.squiddev.plethora.gameplay.neural.NeuralRegistry;

public final class API implements PlethoraGameplayAPI.IPlethoraGameplayAPI {
	@Override
	public INeuralRegistry neuralRegistry() {
		return NeuralRegistry.instance;
	}
}

