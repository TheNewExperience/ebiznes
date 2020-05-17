import React from 'react';
import {
    BrowserRouter as Router,
    Route,
    Link
} from 'react-router-dom';
import Categories from './components//Categories'
import CategoryForm from './components//CategoryForm'
import RegisterUser from "./components/RegisterUser";
import Users from "./components/Users"
import PaymentForm from "./components/PaymentForm";
import Payments from "./components/Payments"
import ManufacturerForm from "./components/ManufacturerForm";
import Manufacturers from "./components/Manufacturers"
// import ProductForm from "./components/ProductForm";
import Products from "./components/Products"


function App() {
    return <Router>
        <div id="menu">
            <ul>
                <li>
                    <Link to="/categoriesjson">Categories</Link>
                </li>
                <li>
                    <Link to="/addcategoryjson">Add Category</Link>
                </li>
                <li>
                    <Link to="/adduserjson">Register user</Link>
                </li>
                <li>
                    <Link to="/usersjson">Users</Link>
                </li>
                <li>
                    <Link to="/productsjson">Products</Link>
                </li>
                <li>
                    <Link to="/manufacturersjson">Manufacturers</Link>
                </li>
                <li>
                    <Link to="/addmanufacturerjson">Add manufacturer</Link>
                </li>
                <li>
                    <Link to="/addpaymentjson">Add payment method</Link>
                </li>
                <li>
                    <Link to="/paymentsjson">Payment methods</Link>
                </li>
            </ul>
            <Route path="/categoriesjson" component={Categories}/>
            <Route path="/addcategoryjson" component={CategoryForm}/>
            <Route path="/adduserjson" component={RegisterUser}/>
            <Route path="/usersjson" component={Users}/>
            <Route path="/productsjson" component={Products}/>
            <Route path="/manufacturersjson" component={Manufacturers}/>
            <Route path="/addmanufacturerjson" component={ManufacturerForm}/>
            <Route path="/addpaymentjson" component={PaymentForm}/>
            <Route path="/paymentsjson" component={Payments}/>
        </div>
    </Router>
}

export default App;
