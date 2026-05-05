package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserOnboardingPutDTO {

	private boolean onboardingCompleted;

	public boolean isOnboardingCompleted() {
		return onboardingCompleted;
	}

	public void setOnboardingCompleted(boolean onboardingCompleted) {
		this.onboardingCompleted = onboardingCompleted;
	}
}