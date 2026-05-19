package de.ullmann.fooddelivery.deliverservice.entity;

public enum DeliveryStatus {

    PENDING {
        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target == DRIVER_ASSIGNED || target == CANCELLED;
        }
    },

    DRIVER_ASSIGNED {
        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target == PICKED_UP || target == CANCELLED;
        }
    },

    PICKED_UP {
        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target == DELIVERED || target == CANCELLED;
        }
    },

    DELIVERED {
        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return false;
        }
    },

    CANCELLED {
        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(DeliveryStatus target);
}
