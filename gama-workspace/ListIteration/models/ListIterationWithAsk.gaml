/**
* Name: ListIterationWithAsk
* Used for benchmarking 
* Author: WoodenMaiden <yann.pomie@ird.fr>
*/

model ListIterationWithAsk

global {
	int list_size <- 50;
	int index <- 0;
	list<my_agent> my_list_of_agents; 
	
	init {
		create my_agent number: list_size returns: __list;
		my_list_of_agents <- __list;
	}
	
	reflex dynamic {
		ask my_list_of_agents parallel: false {
			do update_index;
		}
	}
	
	reflex stop_simulation when: (index >= list_size) {
		do die; // KILL IT WITH FIRE!
	}
}

species my_agent {
	bool update_index {
		index <- index + 1;
		return true;
	}
}

/// Runs an iteration against a list of agents with size N
experiment ReadNAgentsWithAsk {
	parameter 'N' var: list_size min: 1;
	
	output {
		monitor "index    " value: index;
		monitor "list size" value: list_size;
	}
}