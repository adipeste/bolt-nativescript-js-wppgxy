import { Observable } from '@nativescript/core';

export function createViewModel() {
    const viewModel = new Observable();
    viewModel.message = "Tap the button to start your journey!";
    viewModel.count = 0;

    viewModel.onTap = () => {
        viewModel.count++;
        viewModel.set('message', `You've tapped ${viewModel.count} time${viewModel.count === 1 ? '' : 's'}!`);
        console.log(`Button tapped ${viewModel.count} time${viewModel.count === 1 ? '' : 's'}`);
    };

    return viewModel;
}