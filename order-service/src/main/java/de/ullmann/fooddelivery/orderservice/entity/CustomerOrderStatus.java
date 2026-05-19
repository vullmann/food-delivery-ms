package de.ullmann.fooddelivery.orderservice.entity;

public enum CustomerOrderStatus {
    CREATED {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == PENDING;
        }
    },
    PENDING {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == CONFIRMED || target == CANCELLED;
        }
    },

    CONFIRMED {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == PREPARING || target == CANCELLED;
        }
    },

    PREPARING {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == READY_FOR_DELIVERY || target == CANCELLED;
        }
    },

    READY_FOR_DELIVERY {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == DRIVER_ASSIGNED || target == CANCELLED;
        }
    },

    DRIVER_ASSIGNED {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == ON_THE_WAY || target == CANCELLED;
        }
    },

    ON_THE_WAY {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return target == DELIVERED || target == CANCELLED;
        }
    },

    // final states
    DELIVERED {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return false;
        }
    },

    CANCELLED {
        @Override
        public boolean canTransitionTo(CustomerOrderStatus target) {
            return false;
        }
    };

    // abstract method every new state has to implement it
    public abstract boolean canTransitionTo(CustomerOrderStatus target);
}
