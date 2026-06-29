package com.dev.dbaas.common;

import lombok.Getter;
import lombok.Setter;

public abstract class JobBase {

	@Getter
	@Setter
	private long id;
	@Getter
	@Setter
	private long eventTimestamp;
}
