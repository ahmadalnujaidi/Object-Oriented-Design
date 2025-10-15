import java.util.LinkedList;
import java.util.PriorityQueue;

enum State {
    GOING_UP,
    GOING_DOWN,
    IDLE,
}

enum ElevatorType {
    PASSENGER,
    SERVICE,
}

enum RequestOrigin {
    OUTSIDE,
    INSIDE,
}

class Request {

    private RequestOrigin origin;
    private State direction = State.IDLE;
    private int originFloor;
    private int destinationFloor;
    protected ElevatorType elevatorType;

    // Used to send requests that originate from outside the elevator.
    public Request(RequestOrigin origin, int originFloor, int destinationFloor) {
        if (originFloor > destinationFloor) {
            this.direction = State.GOING_DOWN;
        } else if (originFloor < destinationFloor) {
            this.direction = State.GOING_UP;
        }
        this.origin = origin;
        this.originFloor = originFloor;
        this.destinationFloor = destinationFloor;
        this.elevatorType = ElevatorType.PASSENGER;
    }

    // Used to send requests that originate from inside the elevator.
    public Request(RequestOrigin origin, int destinationFloor) {
        this.origin = origin;
        this.destinationFloor = destinationFloor;
    }

    public int getOriginFloor() {
        return this.originFloor;
    }

    public int getDestinationFloor() {
        return this.destinationFloor;
    }

    public RequestOrigin getOrigin() {
        return this.origin;
    }

    public State getDirection() {
        return this.direction;
    }
}

class ServiceRequest extends Request {

    public ServiceRequest(
            RequestOrigin origin,
            int currentFloor,
            int destinationFloor
    ) {
        super(origin, currentFloor, destinationFloor);
        this.elevatorType = ElevatorType.SERVICE;
    }

    public ServiceRequest(RequestOrigin origin, int destinationFloor) {
        super(origin, destinationFloor);
        this.elevatorType = ElevatorType.SERVICE;
    }
}

abstract class Elevator {

    protected int currentFloor;
    protected State state;
    protected boolean emergencyStatus = false;
    private DoorState doorState = DoorState.CLOSED;

    protected enum DoorState {
        OPEN,
        CLOSED,
    }

    public Elevator(int currentFloor, boolean emergencyStatus) {
        this.currentFloor = currentFloor;
        this.state = State.IDLE;
        this.emergencyStatus = emergencyStatus;
    }

    protected void openDoors() {
        doorState = DoorState.OPEN;
        System.out.println("Doors are OPEN on floor " + currentFloor);
    }

    protected void closeDoors() {
        doorState = DoorState.CLOSED;
        System.out.println("Doors are CLOSED");
    }

    protected void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract void operate();

    public abstract void processEmergency();

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public State getState() {
        return state;
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    protected DoorState getDoorState() {
        return this.doorState;
    }

    protected void setEmergencyStatus(boolean status) {
        this.emergencyStatus = status;
    }
}

class PassengerElevator extends Elevator {

    private PriorityQueue<Request> passengerUpQueue;
    private PriorityQueue<Request> passengerDownQueue;

    public PassengerElevator(int currentFloor, boolean emergencyStatus) {
        super(currentFloor, emergencyStatus);
        passengerUpQueue =
                new PriorityQueue<>((a, b) ->
                        a.getDestinationFloor() - b.getDestinationFloor()
                );
        passengerDownQueue =
                new PriorityQueue<>((a, b) ->
                        b.getDestinationFloor() - a.getDestinationFloor()
                );
    }

    @Override
    protected void operate() {
        while (!passengerUpQueue.isEmpty() || !passengerDownQueue.isEmpty()) {
            processRequests();
        }
        this.setState(State.IDLE);
        System.out.println(
                "All requests have been fulfilled, elevator is now " + this.getState()
        );
    }

    @Override
    public void processEmergency() {
        passengerUpQueue.clear();
        passengerDownQueue.clear();

        this.setCurrentFloor(1);
        this.setState(State.IDLE);
        this.openDoors();
        this.setEmergencyStatus(true);
        System.out.println(
                "Queues cleared, current floor is " +
                        this.getCurrentFloor() +
                        ". Doors are " +
                        this.getDoorState()
        );
    }

    public void addUpRequest(Request request) {
        if (request.getOrigin() == RequestOrigin.OUTSIDE) {
            Request pickUpRequest = new Request(
                    request.getOrigin(),
                    request.getOriginFloor(),
                    request.getOriginFloor()
            );
            passengerUpQueue.offer(pickUpRequest);
        }
        passengerUpQueue.offer(request);
    }

    public void addDownRequest(Request request) {
        // if the request is made from the outside
        if (request.getOrigin() == RequestOrigin.OUTSIDE) {
            Request pickUpRequest = new Request(
                    request.getOrigin(),
                    request.getOriginFloor(),
                    request.getOriginFloor()
            );
            passengerDownQueue.offer(pickUpRequest);
        }
        passengerDownQueue.offer(request);
    }

    private void processUpRequests() {
        while (!passengerUpQueue.isEmpty()) {
            Request upRequest = passengerUpQueue.poll();

            if (this.getCurrentFloor() == upRequest.getDestinationFloor()) {
                System.out.println(
                        "Currently on floor " +
                                this.getCurrentFloor() +
                                ". No movement as destination is the same."
                );
                continue;
            }
            System.out.println(
                    "The current floor is " +
                            this.getCurrentFloor() +
                            ". Next stop: " +
                            upRequest.getDestinationFloor()
            );

            try {
                System.out.print("Moving ");
                for (int i = 0; i < 3; i++) {
                    System.out.print(".");
                    Thread.sleep(500); // Pause for half a second between dots.
                }
                Thread.sleep(1000); // Assuming 1 second to move to the next floor.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.setCurrentFloor(upRequest.getDestinationFloor());
            System.out.println("\nArrived at " + this.getCurrentFloor());

            openDoors();
            waitForSeconds(3); // Simulating 3 seconds for people to enter/exit.
            closeDoors();
        }
        System.out.println("Finished processing all the up requests.");
    }

    private void processDownRequests() {
        while (!passengerDownQueue.isEmpty()) {
            Request downRequest = passengerDownQueue.poll();

            if (this.getCurrentFloor() == downRequest.getDestinationFloor()) {
                System.out.println(
                        "Currently on floor " +
                                this.getCurrentFloor() +
                                ". No movement as destination is the same."
                );
                continue;
            }

            System.out.println(
                    "The current floor is " +
                            this.getCurrentFloor() +
                            ". Next stop: " +
                            downRequest.getDestinationFloor()
            );

            try {
                System.out.print("Moving ");
                for (int i = 0; i < 3; i++) {
                    System.out.print(".");
                    Thread.sleep(500); // Pause for half a second between dots.
                }
                Thread.sleep(1000); // Assuming 1 second to move to the next floor.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.setCurrentFloor(downRequest.getDestinationFloor());
            System.out.println("\nArrived at " + this.getCurrentFloor());

            openDoors();
            waitForSeconds(3); // Simulating 3 seconds for people to enter/exit.
            closeDoors();
        }
        System.out.println("Finished processing all the down requests.");
    }

    public void processRequests() {
        if (this.getState() == State.GOING_UP || this.getState() == State.IDLE) {
            processUpRequests();
            if (!passengerDownQueue.isEmpty()) {
                System.out.println("Now processing down requests...");
                processDownRequests();
            }
        } else {
            processDownRequests();
            if (!passengerUpQueue.isEmpty()) {
                System.out.println("Now processing up requests...");
                processUpRequests();
            }
        }
    }
}

class ServiceElevator extends Elevator {

    private LinkedList<ServiceRequest> serviceQueue;

    public ServiceElevator(int currentFloor, boolean emergencyStatus) {
        super(currentFloor, emergencyStatus);
        this.serviceQueue = new LinkedList<>();
    }

    @Override
    public void operate() {
        while (!serviceQueue.isEmpty()) {
            ServiceRequest currRequest = serviceQueue.remove();

            System.out.println(); // Move to the next line after the dots.
            System.out.println("Currently at " + this.getCurrentFloor());
            try {
                Thread.sleep(1000); // Assuming 1 second to move to the next floor.
                System.out.print(currRequest.getDirection());
                for (int i = 0; i < 3; i++) {
                    System.out.print(".");
                    Thread.sleep(500); // Pause for half a second between dots.
                }
            } catch (InterruptedException e) {
                // Handle the interrupted exception here.
                e.printStackTrace();
            }
            this.setCurrentFloor(currRequest.getDestinationFloor());
            this.setState(currRequest.getDirection());
            System.out.println("Arrived at " + this.getCurrentFloor());
            openDoors();
            waitForSeconds(3); // Simulating 3 seconds for loading/unloading.
            closeDoors();
        }
        this.setState(State.IDLE);
        System.out.println(
                "All requests have been fulfilled, elevator is now " + this.getState()
        );
    }

    public void addRequestToQueue(ServiceRequest request) {
        serviceQueue.add(request);
    }

    @Override
    public void processEmergency() {
        serviceQueue.clear();
        this.setCurrentFloor(1);
        this.setState(State.IDLE);
        this.openDoors();
        this.setEmergencyStatus(true);
        System.out.println(
                "Queue cleared, current floor is " +
                        this.getCurrentFloor() +
                        ". Doors are " +
                        this.getDoorState()
        );
    }
}

class ElevatorFactory {

    public Elevator createElevator(ElevatorType type) {
        return switch (type) {
            case PASSENGER -> new PassengerElevator(1, false);
            case SERVICE -> new ServiceElevator(1, false);
            default -> throw new IllegalArgumentException("Unknown criteria.");
        };
    }
}

class Controller {

    private ElevatorFactory factory;
    private PassengerElevator passengerElevator;
    private ServiceElevator serviceElevator;

    public Controller(ElevatorFactory factory) {
        this.factory = factory;
        this.passengerElevator =
                (PassengerElevator) this.factory.createElevator(ElevatorType.PASSENGER);
        this.serviceElevator =
                (ServiceElevator) this.factory.createElevator(ElevatorType.SERVICE);
    }

    public void sendPassengerUpRequests(Request request) {
        this.passengerElevator.addUpRequest(request);
    }

    public void sendPassengerDownRequests(Request request) {
        this.passengerElevator.addDownRequest(request);
    }

    public void sendServiceRequest(ServiceRequest request) {
        this.serviceElevator.addRequestToQueue(request);
    }

    public void handlePassengerRequests() {
        this.passengerElevator.operate();
    }

    public void handleServiceRequests() {
        this.serviceElevator.operate();
    }

    public void handleEmergency() {
        passengerElevator.processEmergency();
        serviceElevator.processEmergency();
    }
}

class ElevatorSystem {

    public static void main(String[] args) {
        ElevatorFactory factory = new ElevatorFactory();
        Controller controller = new Controller(factory);

        controller.sendPassengerUpRequests(
                new Request(RequestOrigin.OUTSIDE, 1, 5)
        );
        controller.sendPassengerDownRequests(
                new Request(RequestOrigin.OUTSIDE, 4, 2)
        );
        controller.sendPassengerUpRequests(
                new Request(RequestOrigin.OUTSIDE, 3, 6)
        );
        controller.handlePassengerRequests();


        controller.sendPassengerUpRequests(
                new Request(RequestOrigin.OUTSIDE, 1, 9)
        );
        controller.sendPassengerDownRequests(new Request(RequestOrigin.INSIDE, 5));
        controller.sendPassengerUpRequests(
                new Request(RequestOrigin.OUTSIDE, 4, 12)
        );
        controller.sendPassengerDownRequests(
                new Request(RequestOrigin.OUTSIDE, 10, 2)
        );
        controller.handlePassengerRequests();


        System.out.println("Now processing service requests");
        controller.sendServiceRequest(new ServiceRequest(RequestOrigin.INSIDE, 13));
        controller.sendServiceRequest(
                new ServiceRequest(RequestOrigin.OUTSIDE, 13, 2)
        );
        controller.sendServiceRequest(
                new ServiceRequest(RequestOrigin.INSIDE, 13, 15)
        );
        controller.handleServiceRequests();
    }
}
