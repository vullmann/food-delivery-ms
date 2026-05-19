package de.ullmann.fooddelivery.restaurantservice.entity;

public enum RestaurantOrderStatus {

    RECEIVED {
        @Override
        public boolean canTransitionTo(RestaurantOrderStatus target) {
            return target == CONFIRMED || target == CANCELLED;
        }
    },

    CONFIRMED {
        @Override
        public boolean canTransitionTo(RestaurantOrderStatus target) {
            return target == PREPARING || target == CANCELLED;
        }
    },

    PREPARING {
        @Override
        public boolean canTransitionTo(RestaurantOrderStatus target) {
            return target == READY_FOR_DELIVERY || target == CANCELLED;
        }
    },

    READY_FOR_DELIVERY {
        @Override
        public boolean canTransitionTo(RestaurantOrderStatus target) {
            return target == PICKED_UP || target == CANCELLED;
        }
    },

    // final states
    PICKED_UP {
        @Override
        public boolean canTransitionTo(RestaurantOrderStatus target) {
            return false;
        }
    },

    CANCELLED {
        @Override
        public boolean canTransitionTo(RestaurantOrderStatus target) {
            return false;
        }
    };

    // abstract method every new state has to implement it
    public abstract boolean canTransitionTo(RestaurantOrderStatus target);
}